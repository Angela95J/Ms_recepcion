import joblib
import numpy as np
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
from typing import Dict, List, Optional
from pathlib import Path


class KMeansImageAnalyzer:
    """Modelo K-means para análisis y clustering de imágenes"""

    def __init__(self, n_clusters: int = 5):
        """Inicializa el analizador de imágenes"""
        self.n_clusters = n_clusters
        self.kmeans = KMeans(n_clusters=n_clusters, random_state=42, n_init=10, max_iter=300)
        self.cluster_to_severity: Dict[int, int] = {}
        self.cluster_labels: Dict[int, str] = {}
        self.is_trained: bool = False

    def train(self, features: np.ndarray, labels: Optional[List[int]] = None) -> Dict:
        """Entrena el modelo K-means"""
        self.kmeans.fit(features)

        silhouette = silhouette_score(features, self.kmeans.labels_)
        inertia = self.kmeans.inertia_

        if labels is not None:
            self._map_clusters_to_severity(self.kmeans.labels_, labels)
        else:
            self._auto_map_clusters(features)

        self._assign_labels()
        self.is_trained = True

        return {
            "silhouette_score": float(silhouette),
            "inertia": float(inertia),
            "n_clusters": self.n_clusters,
            "n_samples": len(features),
            "cluster_mapping": self.cluster_to_severity
        }

    def _map_clusters_to_severity(self, assignments: np.ndarray, true_severities: List[int]):
        """Mapea clusters a severidades"""
        import pandas as pd
        df = pd.DataFrame({'cluster': assignments, 'severity': true_severities})
        for cluster_id in range(self.n_clusters):
            cluster_data = df[df['cluster'] == cluster_id]
            if len(cluster_data) > 0:
                self.cluster_to_severity[cluster_id] = int(cluster_data['severity'].mode()[0])
            else:
                self.cluster_to_severity[cluster_id] = 3

    def _auto_map_clusters(self, features: np.ndarray):
        """Mapeo automático basado en densidad"""
        cluster_densities = []
        for cluster_id in range(self.n_clusters):
            indices = np.where(self.kmeans.labels_ == cluster_id)[0]
            if len(indices) > 0:
                points = features[indices]
                centroid = self.kmeans.cluster_centers_[cluster_id]
                avg_dist = np.mean([np.linalg.norm(p - centroid) for p in points])
                cluster_densities.append((cluster_id, avg_dist))

        cluster_densities.sort(key=lambda x: x[1])
        for severity, (cluster_id, _) in enumerate(cluster_densities, start=1):
            self.cluster_to_severity[cluster_id] = severity

    def _assign_labels(self):
        """Asigna etiquetas descriptivas"""
        labels_map = {
            1: "Escena crítica - Alta severidad",
            2: "Escena urgente - Severidad moderada-alta",
            3: "Escena moderada",
            4: "Escena menor",
            5: "Escena no urgente"
        }
        for cluster_id, severity in self.cluster_to_severity.items():
            self.cluster_labels[cluster_id] = labels_map.get(severity, "Desconocido")

    def predict(self, features: np.ndarray) -> Dict:
        """Predice características de la imagen"""
        if not self.is_trained:
            raise ValueError("Modelo no entrenado")

        cluster_id = self.kmeans.predict([features])[0]
        severidad = self.cluster_to_severity.get(cluster_id, 3)

        distances = self.kmeans.transform([features])[0]
        inv_distances = 1 / (distances + 1e-10)
        probabilities = inv_distances / inv_distances.sum()
        confidence = float(probabilities[cluster_id])

        # Score de anomalía (basado en distancia al centroide)
        distance_to_centroid = distances[cluster_id]
        max_distance = np.max(distances)
        anomaly_score = distance_to_centroid / (max_distance + 1e-10)

        return {
            "cluster_id": int(cluster_id),
            "severidad": severidad,
            "tipo_escena": self.cluster_labels.get(cluster_id, "Desconocido"),
            "confidence": confidence,
            "anomaly_score": float(anomaly_score),
            "is_anomaly": anomaly_score > 0.7
        }

    def save_model(self, model_path: str):
        """Guarda el modelo"""
        Path(model_path).parent.mkdir(parents=True, exist_ok=True)
        data = {
            'kmeans': self.kmeans,
            'cluster_to_severity': self.cluster_to_severity,
            'cluster_labels': self.cluster_labels,
            'n_clusters': self.n_clusters,
            'is_trained': self.is_trained
        }
        joblib.dump(data, model_path)

    def load_model(self, model_path: str):
        """Carga el modelo"""
        data = joblib.load(model_path)
        self.kmeans = data['kmeans']
        self.cluster_to_severity = data['cluster_to_severity']
        self.cluster_labels = data['cluster_labels']
        self.n_clusters = data['n_clusters']
        self.is_trained = data['is_trained']
