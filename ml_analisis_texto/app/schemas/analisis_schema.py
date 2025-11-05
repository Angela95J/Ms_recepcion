from pydantic import BaseModel, Field, UUID4
from typing import Dict, List, Optional
from datetime import datetime
from decimal import Decimal


class AnalizarTextoRequest(BaseModel):
    """Request para analizar texto de incidente"""
    texto: str = Field(..., min_length=10, max_length=5000, description="Descripción del incidente")
    incidente_id: Optional[UUID4] = Field(None, description="ID del incidente (opcional)")


class AnalizarTextoResponse(BaseModel):
    """Response con el análisis del texto"""

    # Resultados principales
    prioridad_calculada: int = Field(..., ge=1, le=5, description="Prioridad del 1 (más urgente) al 5 (menos urgente)")
    nivel_gravedad: int = Field(..., ge=1, le=5, description="Nivel de gravedad detectado")
    tipo_incidente_predicho: str = Field(..., description="Tipo de incidente clasificado")

    # Análisis detallado
    categorias_detectadas: Dict[str, float] = Field(..., description="Categorías detectadas con probabilidades")
    palabras_clave_criticas: List[str] = Field(..., description="Palabras clave críticas encontradas")
    entidades_medicas: Optional[Dict[str, List[str]]] = Field(None, description="Entidades médicas identificadas")

    # Métricas del modelo
    score_confianza: Decimal = Field(..., ge=0, le=1, description="Confianza del modelo (0-1)")
    probabilidades_categorias: Dict[str, float] = Field(..., description="Distribución de probabilidades")

    # Metadata
    modelo_version: str = Field(default="kmeans-v1.0", description="Versión del modelo")
    algoritmo_usado: str = Field(default="kmeans", description="Algoritmo utilizado")
    tiempo_procesamiento_ms: int = Field(..., description="Tiempo de procesamiento en ms")
    fecha_analisis: datetime = Field(default_factory=datetime.now, description="Timestamp del análisis")

    class Config:
        json_schema_extra = {
            "example": {
                "prioridad_calculada": 1,
                "nivel_gravedad": 5,
                "tipo_incidente_predicho": "Accidente de tráfico con heridos graves",
                "categorias_detectadas": {
                    "trauma": 0.95,
                    "urgencia_vital": 0.89,
                    "accidente_transito": 0.92
                },
                "palabras_clave_criticas": ["heridos", "sangre", "inconsciente", "ambulancia urgente"],
                "entidades_medicas": {
                    "sintomas": ["heridas", "sangrado", "inconsciencia"],
                    "partes_cuerpo": ["cabeza", "torso"]
                },
                "score_confianza": 0.92,
                "probabilidades_categorias": {
                    "cluster_0": 0.92,
                    "cluster_1": 0.05,
                    "cluster_2": 0.02,
                    "cluster_3": 0.01,
                    "cluster_4": 0.00
                },
                "modelo_version": "kmeans-v1.0",
                "algoritmo_usado": "kmeans",
                "tiempo_procesamiento_ms": 45
            }
        }


class HealthCheckResponse(BaseModel):
    """Response del health check"""
    status: str = Field(..., description="Estado del servicio")
    model_loaded: bool = Field(..., description="Si el modelo está cargado")
    model_version: str = Field(..., description="Versión del modelo")
    timestamp: datetime = Field(default_factory=datetime.now)


class ReentrenarRequest(BaseModel):
    """Request para reentrenar el modelo"""
    num_clusters: Optional[int] = Field(5, ge=2, le=10, description="Número de clusters")
    min_samples: Optional[int] = Field(100, ge=50, description="Mínimo de muestras requeridas")


class ReentrenarResponse(BaseModel):
    """Response del reentrenamiento"""
    success: bool
    message: str
    samples_used: int
    clusters_created: int
    model_version: str
    metrics: Optional[Dict[str, float]] = None
