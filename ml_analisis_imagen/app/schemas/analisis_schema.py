from pydantic import BaseModel, Field, UUID4
from typing import Dict, List, Optional
from datetime import datetime
from decimal import Decimal
from enum import Enum


class CalidadImagen(str, Enum):
    """Enum para calidad de imagen"""
    EXCELENTE = "EXCELENTE"
    BUENA = "BUENA"
    REGULAR = "REGULAR"
    MALA = "MALA"


class AnalizarImagenRequest(BaseModel):
    """Request para analizar imagen"""
    imagen_path: str = Field(..., description="Ruta de la imagen a analizar")
    multimedia_id: Optional[UUID4] = Field(None, description="ID del multimedia")
    incidente_id: Optional[UUID4] = Field(None, description="ID del incidente")


class AnalizarImagenResponse(BaseModel):
    """Response con el análisis de imagen"""

    # Resultados principales
    es_imagen_accidente: bool = Field(..., description="Si es imagen de accidente real")
    score_veracidad: Decimal = Field(..., ge=0, le=1, description="Score de veracidad (0-1)")
    tipo_escena_detectada: Optional[str] = Field(None, description="Tipo de escena")
    nivel_gravedad_visual: Optional[int] = Field(None, ge=1, le=5, description="Gravedad visual (1-5)")

    # Detección de elementos
    elementos_criticos_detectados: Dict[str, float] = Field(..., description="Elementos críticos")
    objetos_detectados: Dict[str, int] = Field(..., description="Objetos detectados")
    personas_detectadas: int = Field(default=0, description="Cantidad de personas")
    vehiculos_detectados: int = Field(default=0, description="Cantidad de vehículos")

    # Clasificación de escena
    categorias_escena: Dict[str, float] = Field(..., description="Categorías de escena")
    score_confianza_escena: Decimal = Field(..., ge=0, le=1, description="Confianza clasificación")

    # Detección de anomalías
    es_anomalia: bool = Field(default=False, description="Si es anomalía")
    score_anomalia: Optional[Decimal] = Field(None, ge=0, le=1, description="Score anomalía")
    razon_sospecha: Optional[str] = Field(None, description="Razón de sospecha")

    # Calidad de imagen
    calidad_imagen: CalidadImagen = Field(..., description="Calidad de la imagen")
    resolucion_imagen: str = Field(..., description="Resolución WxH")
    es_imagen_clara: bool = Field(..., description="Si la imagen es clara")

    # Metadata
    modelo_vision: str = Field(default="kmeans-features", description="Modelo usado")
    modelo_veracidad: str = Field(default="kmeans-anomaly", description="Modelo veracidad")
    tiempo_procesamiento_ms: int = Field(..., description="Tiempo procesamiento")
    fecha_analisis: datetime = Field(default_factory=datetime.now)

    class Config:
        json_schema_extra = {
            "example": {
                "es_imagen_accidente": True,
                "score_veracidad": 0.87,
                "tipo_escena_detectada": "Accidente vehicular",
                "nivel_gravedad_visual": 4,
                "elementos_criticos_detectados": {
                    "vehiculo_danado": 0.92,
                    "personas": 0.78,
                    "escombros": 0.65
                },
                "objetos_detectados": {
                    "vehiculos": 2,
                    "personas": 1
                },
                "personas_detectadas": 1,
                "vehiculos_detectados": 2,
                "categorias_escena": {
                    "accidente": 0.87,
                    "via_publica": 0.65,
                    "emergencia": 0.72
                },
                "score_confianza_escena": 0.85,
                "es_anomalia": False,
                "score_anomalia": 0.15,
                "calidad_imagen": "BUENA",
                "resolucion_imagen": "1920x1080",
                "es_imagen_clara": True,
                "tiempo_procesamiento_ms": 250
            }
        }


class HealthCheckResponse(BaseModel):
    """Response del health check"""
    status: str
    model_loaded: bool
    model_version: str
    timestamp: datetime = Field(default_factory=datetime.now)


class ReentrenarRequest(BaseModel):
    """Request para reentrenar"""
    num_clusters: Optional[int] = Field(5, ge=2, le=10)
    min_samples: Optional[int] = Field(100, ge=50)


class ReentrenarResponse(BaseModel):
    """Response del reentrenamiento"""
    success: bool
    message: str
    samples_used: int
    clusters_created: int
    model_version: str
    metrics: Optional[Dict[str, float]] = None
