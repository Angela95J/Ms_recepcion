"""
Script para entrenar el modelo K-means de análisis de imágenes

NOTA: Este script usa imágenes sintéticas para demostración.
En producción, usar dataset real de imágenes de incidentes.
"""

import sys
import numpy as np
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))

from app.models.kmeans_image_model import KMeansImageAnalyzer
from app.config import settings


def generate_synthetic_features(n_samples: int, n_features: int = 150) -> np.ndarray:
    """
    Genera características sintéticas que simulan features de imágenes

    En producción, esto vendría de extract_features() de imágenes reales
    """
    np.random.seed(42)

    features = []

    # Cluster 0: Escenas críticas (alta severidad)
    critical = np.random.normal(loc=0.8, scale=0.1, size=(n_samples // 5, n_features))
    features.append(critical)

    # Cluster 1: Escenas urgentes
    urgent = np.random.normal(loc=0.6, scale=0.15, size=(n_samples // 5, n_features))
    features.append(urgent)

    # Cluster 2: Escenas moderadas
    moderate = np.random.normal(loc=0.4, scale=0.1, size=(n_samples // 5, n_features))
    features.append(moderate)

    # Cluster 3: Escenas menores
    minor = np.random.normal(loc=0.2, scale=0.1, size=(n_samples // 5, n_features))
    features.append(minor)

    # Cluster 4: Escenas normales
    normal = np.random.normal(loc=0.05, scale=0.05, size=(n_samples // 5, n_features))
    features.append(normal)

    all_features = np.vstack(features)
    labels = np.array([1] * (n_samples // 5) +
                     [2] * (n_samples // 5) +
                     [3] * (n_samples // 5) +
                     [4] * (n_samples // 5) +
                     [5] * (n_samples // 5))

    # Mezclar
    indices = np.random.permutation(len(all_features))
    return all_features[indices], labels[indices]


def main():
    """Función principal de entrenamiento"""
    print("=" * 60)
    print("ENTRENAMIENTO DEL MODELO K-MEANS PARA ANÁLISIS DE IMÁGENES")
    print("=" * 60)

    # 1. Generar features sintéticos
    print("\n[1/4] Generando características sintéticas...")
    n_samples = 500
    features, labels = generate_synthetic_features(n_samples)

    print(f"   - Total de muestras: {n_samples}")
    print(f"   - Dimensión de features: {features.shape[1]}")
    print(f"   - Distribución de severidades:")
    for severity in range(1, 6):
        count = np.sum(labels == severity)
        print(f"     Severidad {severity}: {count} muestras")

    # 2. Inicializar modelo
    print("\n[2/4] Inicializando modelo K-means...")
    model = KMeansImageAnalyzer(n_clusters=settings.num_clusters)

    # 3. Entrenar
    print("\n[3/4] Entrenando modelo...")
    metrics = model.train(features, labels.tolist())

    print(f"   OK - Entrenamiento completado")
    print(f"   - Silhouette Score: {metrics['silhouette_score']:.4f}")
    print(f"   - Inertia: {metrics['inertia']:.2f}")
    print(f"   - Clusters creados: {metrics['n_clusters']}")

    print("\n   Mapeo de clusters a severidades:")
    for cluster_id, severity in metrics['cluster_mapping'].items():
        label = model.cluster_labels[cluster_id]
        print(f"   Cluster {cluster_id} -> Severidad {severity}: {label}")

    # 4. Guardar modelo
    print("\n[4/4] Guardando modelo...")
    model_path = Path(settings.model_path)
    model.save_model(str(model_path))
    print(f"   OK - Modelo guardado en: {model_path}")

    # Prueba rápida
    print("\n" + "=" * 60)
    print("PRUEBA DEL MODELO")
    print("=" * 60)

    # Generar features de prueba directamente
    np.random.seed(999)  # Diferente seed para pruebas
    test_critical = np.random.normal(loc=0.8, scale=0.1, size=features.shape[1])
    test_normal = np.random.normal(loc=0.05, scale=0.05, size=features.shape[1])

    test_features = [
        test_critical,  # Escena crítica
        test_normal,  # Escena normal
    ]

    test_labels = ["Escena critica (simulada)", "Escena normal (simulada)"]

    for i, (test_feat, label) in enumerate(zip(test_features, test_labels)):
        result = model.predict(test_feat)
        print(f"\nTest {i+1}: {label}")
        print(f"  -> Severidad: {result['severidad']}")
        print(f"  -> Tipo: {result['tipo_escena']}")
        print(f"  -> Confianza: {result['confidence']:.2%}")
        print(f"  -> Anomalia: {result['is_anomaly']} (score: {result['anomaly_score']:.2f})")

    print("\n" + "=" * 60)
    print("ENTRENAMIENTO COMPLETADO")
    print("=" * 60)
    print("\nNOTA: Este modelo usa datos sintéticos para demostración.")
    print("En producción, entrena con imágenes reales de incidentes.")
    print("\nInicia el servidor con:")
    print("  python -m uvicorn app.main:app --reload --port 8002")


if __name__ == "__main__":
    main()
