import time
from datetime import datetime
from decimal import Decimal
from typing import Dict, Optional
from pathlib import Path

from app.models.kmeans_image_model import KMeansImageAnalyzer
from app.utils.image_preprocessing import ImagePreprocessor
from app.schemas.analisis_schema import AnalizarImagenResponse, CalidadImagen
from app.config import settings


class AnalisisImagenService:
    """Servicio de análisis de imágenes"""

    def __init__(self):
        """Inicializa el servicio"""
        self.preprocessor = ImagePreprocessor(
            target_size=(settings.image_resize_width, settings.image_resize_height)
        )
        self.model = KMeansImageAnalyzer(n_clusters=settings.num_clusters)
        self.model_loaded = False
        self.model_version = settings.model_version

        self._load_model_if_exists()

    def _load_model_if_exists(self):
        """Carga el modelo si existe"""
        try:
            model_path = Path(settings.model_path)
            if model_path.exists():
                self.model.load_model(str(model_path))
                self.model_loaded = True
                print(f"OK - Modelo cargado: {self.model_version}")
            else:
                print(f"AVISO - Modelo no encontrado. Entrena el modelo primero.")
                self.model_loaded = False
        except Exception as e:
            print(f"ERROR - Error al cargar modelo: {e}")
            self.model_loaded = False

    def analizar_imagen(self, imagen_path: str, multimedia_id: Optional[str] = None,
                       incidente_id: Optional[str] = None) -> AnalizarImagenResponse:
        """Analiza una imagen y retorna resultados"""
        if not self.model_loaded:
            raise ValueError("Modelo no entrenado")

        start_time = time.time()

        # 1. Cargar imagen original
        img_original = self.preprocessor.load_image(imagen_path)

        # 2. Obtener info de imagen
        img_info = self.preprocessor.get_image_info(img_original)
        calidad = self.preprocessor.get_image_quality(img_original)
        es_clara = self.preprocessor.is_image_clear(img_original)

        # 3. Detectar objetos simples
        objetos = self.preprocessor.detect_simple_objects(img_original)

        # 4. Extraer características y predecir
        features = self.preprocessor.extract_features(img_original)
        prediccion = self.model.predict(features)

        # 5. Calcular score de veracidad (inverso de anomalía)
        score_veracidad = 1.0 - prediccion["anomaly_score"]

        # 6. Determinar si es imagen de accidente (heurística simple)
        es_accidente = prediccion["severidad"] <= 3 and score_veracidad > 0.5

        # 7. Estimar personas y vehículos (simplificado)
        personas_estimadas = min(objetos["objetos_grandes"], 5)
        vehiculos_estimados = min(objetos["objetos_medianos"], 3) if es_accidente else 0

        # 8. Categorizar escena
        categorias = self._categorizar_escena(prediccion, objetos)

        # 9. Detectar elementos críticos
        elementos_criticos = self._detectar_elementos_criticos(prediccion, objetos)

        # 10. Calcular tiempo
        tiempo_ms = int((time.time() - start_time) * 1000)

        response = AnalizarImagenResponse(
            es_imagen_accidente=es_accidente,
            score_veracidad=Decimal(str(round(score_veracidad, 4))),
            tipo_escena_detectada=prediccion["tipo_escena"],
            nivel_gravedad_visual=prediccion["severidad"],
            elementos_criticos_detectados=elementos_criticos,
            objetos_detectados=objetos,
            personas_detectadas=personas_estimadas,
            vehiculos_detectados=vehiculos_estimados,
            categorias_escena=categorias,
            score_confianza_escena=Decimal(str(round(prediccion["confidence"], 4))),
            es_anomalia=prediccion["is_anomaly"],
            score_anomalia=Decimal(str(round(prediccion["anomaly_score"], 4))),
            razon_sospecha="Imagen atípica detectada" if prediccion["is_anomaly"] else None,
            calidad_imagen=CalidadImagen(calidad),
            resolucion_imagen=img_info["resolution"],
            es_imagen_clara=es_clara,
            modelo_vision="kmeans-features",
            modelo_veracidad="kmeans-anomaly",
            tiempo_procesamiento_ms=tiempo_ms,
            fecha_analisis=datetime.now()
        )

        return response

    def _categorizar_escena(self, prediccion: Dict, objetos: Dict) -> Dict[str, float]:
        """Categoriza la escena"""
        categorias = {}

        severidad = prediccion["severidad"]
        num_objetos = objetos["total_objetos"]

        if severidad <= 2:
            categorias["accidente"] = 0.8
            categorias["emergencia"] = 0.7
        elif severidad == 3:
            categorias["incidente"] = 0.6
            categorias["precaucion"] = 0.5
        else:
            categorias["normal"] = 0.7

        if num_objetos > 5:
            categorias["via_publica"] = 0.6
            categorias["multiples_elementos"] = 0.5

        return categorias

    def _detectar_elementos_criticos(self, prediccion: Dict, objetos: Dict) -> Dict[str, float]:
        """Detecta elementos críticos"""
        elementos = {}

        if prediccion["severidad"] <= 2:
            elementos["situacion_critica"] = 0.8

        if objetos["objetos_grandes"] > 0:
            elementos["objetos_grandes"] = 0.7

        if objetos["objetos_medianos"] > 2:
            elementos["vehiculos_posibles"] = 0.6

        return elementos

    def health_check(self) -> Dict:
        """Health check"""
        return {
            "status": "healthy" if self.model_loaded else "model_not_loaded",
            "model_loaded": self.model_loaded,
            "model_version": self.model_version,
            "timestamp": datetime.now()
        }


# Instancia global
analisis_service = AnalisisImagenService()
