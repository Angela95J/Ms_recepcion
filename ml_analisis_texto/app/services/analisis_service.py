import time
from datetime import datetime
from decimal import Decimal
from typing import Dict, Optional
from pathlib import Path

from app.models.kmeans_text_model import KMeansTextAnalyzer
from app.utils.text_preprocessing import TextPreprocessor
from app.schemas.analisis_schema import AnalizarTextoResponse
from app.config import settings


class AnalisisTextoService:
    """Servicio de análisis de texto de incidentes"""

    def __init__(self):
        """Inicializa el servicio"""
        self.preprocessor = TextPreprocessor()
        self.model = KMeansTextAnalyzer(n_clusters=settings.num_clusters)
        self.model_loaded = False
        self.model_version = settings.model_version

        # Intentar cargar modelo preentrenado
        self._load_model_if_exists()

    def _load_model_if_exists(self):
        """Carga el modelo si existe"""
        try:
            model_path = Path(settings.model_path)
            vectorizer_path = Path(settings.vectorizer_path)

            if model_path.exists() and vectorizer_path.exists():
                self.model.load_model(str(model_path), str(vectorizer_path))
                self.model_loaded = True
                print(f"OK - Modelo cargado exitosamente: {self.model_version}")
            else:
                print(f"AVISO - No se encontro modelo preentrenado. El modelo debe ser entrenado antes de usar.")
                self.model_loaded = False
        except Exception as e:
            print(f"ERROR - Error al cargar modelo: {e}")
            self.model_loaded = False

    def analizar_texto(self, texto: str, incidente_id: Optional[str] = None) -> AnalizarTextoResponse:
        """
        Analiza el texto del incidente y retorna resultados

        Args:
            texto: Descripción del incidente
            incidente_id: ID del incidente (opcional)

        Returns:
            AnalizarTextoResponse con análisis completo
        """
        if not self.model_loaded:
            raise ValueError("El modelo no está entrenado. Por favor entrena el modelo primero.")

        start_time = time.time()

        # 1. Extraer palabras clave críticas del texto original
        palabras_clave = self.preprocessor.extract_critical_keywords(texto)

        # 2. Extraer entidades médicas
        entidades_medicas = self.preprocessor.extract_medical_entities(texto)

        # 3. Preprocesar texto
        texto_procesado = self.preprocessor.preprocess(texto, apply_stemming=True)

        # 4. Predecir con el modelo
        prediccion = self.model.predict(texto_procesado)

        # 5. Calcular tiempo de procesamiento
        tiempo_ms = int((time.time() - start_time) * 1000)

        # 6. Ajustar prioridad según palabras clave críticas (boost)
        prioridad_ajustada = self._ajustar_prioridad_por_keywords(
            prediccion["prioridad_calculada"],
            palabras_clave
        )

        # 7. Construir response
        response = AnalizarTextoResponse(
            prioridad_calculada=prioridad_ajustada,
            nivel_gravedad=self._calcular_nivel_gravedad(palabras_clave, prioridad_ajustada),
            tipo_incidente_predicho=prediccion["tipo_incidente_predicho"],
            categorias_detectadas=prediccion["categorias_detectadas"],
            palabras_clave_criticas=palabras_clave,
            entidades_medicas=entidades_medicas if entidades_medicas else None,
            score_confianza=Decimal(str(round(prediccion["score_confianza"], 4))),
            probabilidades_categorias=prediccion["probabilidades_categorias"],
            modelo_version=self.model_version,
            algoritmo_usado="kmeans",
            tiempo_procesamiento_ms=tiempo_ms,
            fecha_analisis=datetime.now()
        )

        return response

    def _ajustar_prioridad_por_keywords(self, prioridad_base: int, palabras_clave: list) -> int:
        """
        Ajusta la prioridad según palabras clave críticas encontradas

        Palabras clave muy críticas pueden aumentar la prioridad
        """
        # Palabras que indican máxima urgencia
        maxima_urgencia = {
            'inconsciente', 'no respira', 'no responde', 'paro', 'cardiaco',
            'hemorragia', 'sangre', 'atrapado', 'ahogando'
        }

        # Alta urgencia
        alta_urgencia = {
            'herido', 'grave', 'fractura', 'quemadura', 'accidente',
            'choque', 'atropellado', 'urgente', 'emergencia'
        }

        # Contar coincidencias
        count_maxima = sum(1 for kw in palabras_clave if kw in maxima_urgencia)
        count_alta = sum(1 for kw in palabras_clave if kw in alta_urgencia)

        # Ajustar prioridad
        if count_maxima >= 2:
            # Múltiples keywords de máxima urgencia -> Prioridad 1
            return 1
        elif count_maxima >= 1:
            # Al menos una keyword de máxima urgencia -> Prioridad 1 o 2
            return min(prioridad_base, 2)
        elif count_alta >= 3:
            # Muchas keywords de alta urgencia
            return max(min(prioridad_base - 1, 2), 1)
        elif count_alta >= 1:
            # Al menos una keyword de alta urgencia
            return max(prioridad_base, 3)

        return prioridad_base

    def _calcular_nivel_gravedad(self, palabras_clave: list, prioridad: int) -> int:
        """
        Calcula el nivel de gravedad basado en keywords y prioridad

        Nivel de gravedad puede diferir de prioridad según contexto
        """
        # Indicadores de gravedad extrema
        gravedad_extrema = {
            'inconsciente', 'paro', 'hemorragia', 'no respira',
            'critico', 'grave'
        }

        # Indicadores de gravedad alta
        gravedad_alta = {
            'herido', 'sangre', 'fractura', 'quemadura',
            'accidente', 'choque', 'atropellado'
        }

        count_extrema = sum(1 for kw in palabras_clave if kw in gravedad_extrema)
        count_alta = sum(1 for kw in palabras_clave if kw in gravedad_alta)

        if count_extrema >= 1:
            return 5  # Gravedad máxima
        elif count_alta >= 2:
            return 4
        elif count_alta >= 1:
            return 3
        else:
            # Basar en prioridad
            return max(6 - prioridad, 1)

    def health_check(self) -> Dict:
        """Verifica el estado del servicio"""
        return {
            "status": "healthy" if self.model_loaded else "model_not_loaded",
            "model_loaded": self.model_loaded,
            "model_version": self.model_version,
            "timestamp": datetime.now()
        }

    def get_model_info(self) -> Dict:
        """Obtiene información del modelo"""
        if not self.model_loaded:
            return {
                "loaded": False,
                "message": "Modelo no cargado"
            }

        return {
            "loaded": True,
            "version": self.model_version,
            "n_clusters": self.model.n_clusters,
            "cluster_mapping": self.model.cluster_to_priority,
            "cluster_labels": self.model.cluster_labels
        }


# Instancia global del servicio
analisis_service = AnalisisTextoService()
