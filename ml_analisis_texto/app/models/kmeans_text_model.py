import joblib
import numpy as np
from sklearn.cluster import KMeans
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics import silhouette_score
from typing import Dict, List, Tuple, Optional
import pandas as pd
from datetime import datetime
from pathlib import Path


class KMeansTextAnalyzer:
    """Modelo K-means para análisis y clustering de texto de incidentes"""

    def __init__(self, n_clusters: int = 5):
        """
        Inicializa el analizador

        Args:
            n_clusters: Número de clusters (prioridades)
        """
        self.n_clusters = n_clusters
        self.vectorizer = TfidfVectorizer(
            max_features=500,
            ngram_range=(1, 3),  # Unigramas, bigramas y trigramas
            min_df=2,
            max_df=0.8
        )
        self.kmeans = KMeans(
            n_clusters=n_clusters,
            random_state=42,
            n_init=10,
            max_iter=300
        )
        self.cluster_to_priority: Dict[int, int] = {}
        self.cluster_labels: Dict[int, str] = {}
        self.is_trained: bool = False

    def train(self, texts: List[str], labels: Optional[List[int]] = None) -> Dict:
        """
        Entrena el modelo K-means con textos

        Args:
            texts: Lista de textos preprocesados
            labels: Labels opcionales de prioridad (1-5) para mapeo supervisado

        Returns:
            Dict con métricas del entrenamiento
        """
        # Vectorizar textos
        X = self.vectorizer.fit_transform(texts)

        # Entrenar K-means
        self.kmeans.fit(X)

        # Calcular métricas
        silhouette = silhouette_score(X, self.kmeans.labels_)
        inertia = self.kmeans.inertia_

        # Mapear clusters a prioridades
        if labels is not None:
            self._map_clusters_to_priorities(self.kmeans.labels_, labels)
        else:
            # Mapeo automático basado en distancia al centroide
            self._auto_map_clusters_to_priorities(X)

        # Asignar etiquetas descriptivas a los clusters
        self._assign_cluster_labels()

        self.is_trained = True

        return {
            "silhouette_score": float(silhouette),
            "inertia": float(inertia),
            "n_clusters": self.n_clusters,
            "n_samples": len(texts),
            "cluster_mapping": self.cluster_to_priority,
            "cluster_labels": self.cluster_labels
        }

    def _map_clusters_to_priorities(self, cluster_assignments: np.ndarray, true_priorities: List[int]):
        """Mapea clusters a prioridades basado en datos etiquetados"""
        df = pd.DataFrame({
            'cluster': cluster_assignments,
            'priority': true_priorities
        })

        # Para cada cluster, asignar la prioridad más frecuente
        for cluster_id in range(self.n_clusters):
            cluster_data = df[df['cluster'] == cluster_id]
            if len(cluster_data) > 0:
                most_common_priority = cluster_data['priority'].mode()[0]
                self.cluster_to_priority[cluster_id] = int(most_common_priority)
            else:
                # Si no hay datos para este cluster, asignar prioridad media
                self.cluster_to_priority[cluster_id] = 3

    def _auto_map_clusters_to_priorities(self, X):
        """Mapeo automático: clusters con textos más 'densos' son más urgentes"""
        # Calcular la distancia promedio de cada cluster a su centroide
        cluster_densities = []

        for cluster_id in range(self.n_clusters):
            cluster_indices = np.where(self.kmeans.labels_ == cluster_id)[0]
            if len(cluster_indices) > 0:
                cluster_points = X[cluster_indices]
                centroid = self.kmeans.cluster_centers_[cluster_id]
                avg_distance = np.mean([
                    np.linalg.norm(cluster_points[i].toarray() - centroid)
                    for i in range(len(cluster_indices))
                ])
                cluster_densities.append((cluster_id, avg_distance))

        # Ordenar por densidad (menor distancia = más compacto = más urgente)
        cluster_densities.sort(key=lambda x: x[1])

        # Asignar prioridades (1 = más urgente, 5 = menos urgente)
        for priority, (cluster_id, _) in enumerate(cluster_densities, start=1):
            self.cluster_to_priority[cluster_id] = priority

    def _assign_cluster_labels(self):
        """Asigna etiquetas descriptivas a cada cluster basado en términos más frecuentes"""
        priority_labels = {
            1: "Crítico - Riesgo vital inmediato",
            2: "Urgente - Requiere atención inmediata",
            3: "Moderado - Atención necesaria",
            4: "Menor - Puede esperar",
            5: "No urgente - Atención diferida"
        }

        for cluster_id, priority in self.cluster_to_priority.items():
            self.cluster_labels[cluster_id] = priority_labels.get(priority, "Desconocido")

    def predict(self, text: str, preprocessed: bool = False) -> Dict:
        """
        Predice la prioridad y características del texto

        Args:
            text: Texto a analizar
            preprocessed: Si el texto ya está preprocesado

        Returns:
            Dict con análisis completo
        """
        if not self.is_trained:
            raise ValueError("El modelo no ha sido entrenado. Llama a train() primero.")

        # Vectorizar texto
        X = self.vectorizer.transform([text])

        # Predecir cluster
        cluster_id = self.kmeans.predict(X)[0]

        # Obtener prioridad
        prioridad = self.cluster_to_priority.get(cluster_id, 3)

        # Calcular distancias a todos los centroides
        distances = self.kmeans.transform(X)[0]

        # Calcular probabilidades (inverso de distancias normalizadas)
        inv_distances = 1 / (distances + 1e-10)
        probabilities = inv_distances / inv_distances.sum()

        # Confianza (probabilidad del cluster asignado)
        confidence = float(probabilities[cluster_id])

        # Distribución de probabilidades por cluster
        prob_distribution = {
            f"cluster_{i}": float(probabilities[i])
            for i in range(self.n_clusters)
        }

        # Categorías detectadas (basado en términos TF-IDF más importantes)
        feature_names = self.vectorizer.get_feature_names_out()
        feature_scores = X.toarray()[0]
        top_features_idx = feature_scores.argsort()[-10:][::-1]
        top_features = [feature_names[i] for i in top_features_idx if feature_scores[i] > 0]

        categorias = self._classify_categories(top_features)

        return {
            "cluster_id": int(cluster_id),
            "prioridad_calculada": prioridad,
            "nivel_gravedad": prioridad,  # Por ahora, mismo valor
            "tipo_incidente_predicho": self.cluster_labels.get(cluster_id, "Desconocido"),
            "score_confianza": confidence,
            "probabilidades_categorias": prob_distribution,
            "categorias_detectadas": categorias,
            "top_features": top_features
        }

    def _classify_categories(self, features: List[str]) -> Dict[str, float]:
        """Clasifica en categorías predefinidas basado en features"""
        categories = {
            "trauma": 0.0,
            "urgencia_vital": 0.0,
            "accidente_transito": 0.0,
            "medico": 0.0,
            "caida": 0.0
        }

        # Palabras clave por categoría
        trauma_keywords = ['herido', 'sangre', 'fractur', 'golpe', 'lesion', 'trauma']
        urgencia_keywords = ['urgente', 'critico', 'grave', 'inconscient', 'paro', 'respir']
        transito_keywords = ['accident', 'choque', 'colision', 'atropell', 'vehicul', 'auto']
        medico_keywords = ['dolor', 'enferm', 'diabetic', 'embaraz', 'medic', 'hospit']
        caida_keywords = ['caid', 'tropiez', 'resbalo', 'altura']

        all_features_text = ' '.join(features).lower()

        # Calcular score por categoría
        for keyword in trauma_keywords:
            if keyword in all_features_text:
                categories["trauma"] += 0.2

        for keyword in urgencia_keywords:
            if keyword in all_features_text:
                categories["urgencia_vital"] += 0.2

        for keyword in transito_keywords:
            if keyword in all_features_text:
                categories["accidente_transito"] += 0.2

        for keyword in medico_keywords:
            if keyword in all_features_text:
                categories["medico"] += 0.2

        for keyword in caida_keywords:
            if keyword in all_features_text:
                categories["caida"] += 0.2

        # Normalizar scores
        max_score = max(categories.values()) if max(categories.values()) > 0 else 1
        categories = {k: min(v / max_score, 1.0) for k, v in categories.items()}

        # Filtrar categorías con score > 0
        return {k: v for k, v in categories.items() if v > 0.1}

    def save_model(self, model_path: str, vectorizer_path: str):
        """Guarda el modelo y vectorizador entrenados"""
        Path(model_path).parent.mkdir(parents=True, exist_ok=True)
        Path(vectorizer_path).parent.mkdir(parents=True, exist_ok=True)

        model_data = {
            'kmeans': self.kmeans,
            'cluster_to_priority': self.cluster_to_priority,
            'cluster_labels': self.cluster_labels,
            'n_clusters': self.n_clusters,
            'is_trained': self.is_trained
        }

        joblib.dump(model_data, model_path)
        joblib.dump(self.vectorizer, vectorizer_path)

    def load_model(self, model_path: str, vectorizer_path: str):
        """Carga el modelo y vectorizador previamente entrenados"""
        model_data = joblib.load(model_path)
        self.kmeans = model_data['kmeans']
        self.cluster_to_priority = model_data['cluster_to_priority']
        self.cluster_labels = model_data['cluster_labels']
        self.n_clusters = model_data['n_clusters']
        self.is_trained = model_data['is_trained']

        self.vectorizer = joblib.load(vectorizer_path)
