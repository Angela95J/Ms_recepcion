"""
Script para entrenar el modelo K-means con datos de ejemplo

Este script genera datos sintéticos de incidentes y entrena el modelo.
En producción, debería usar datos reales de la base de datos.
"""

import sys
from pathlib import Path

# Agregar el directorio raíz al path
sys.path.insert(0, str(Path(__file__).parent))

from app.models.kmeans_text_model import KMeansTextAnalyzer
from app.utils.text_preprocessing import TextPreprocessor
from app.config import settings


# Datos de entrenamiento sintéticos (en producción, obtener de BD)
TRAINING_DATA = [
    # PRIORIDAD 1 - CRÍTICO (Riesgo vital inmediato)
    ("Persona inconsciente en la calle, no responde, necesitamos ambulancia urgente", 1),
    ("Accidente de auto grave, conductor atrapado y sangrando mucho, heridas en la cabeza", 1),
    ("Mi esposo tiene dolor en el pecho muy fuerte, no puede respirar bien, creo que es infarto", 1),
    ("Niño se cayó de la escalera, está inconsciente, tiene sangre en la cabeza, ayuda urgente", 1),
    ("Atropellaron a una persona, está tirada en la calle con hemorragia, no se mueve", 1),
    ("Hombre con convulsiones, no responde, está temblando mucho, necesito ayuda ya", 1),
    ("Bebé recién nacido no respira bien, se está poniendo azul, emergencia", 1),
    ("Choque de dos autos, hay varios heridos graves, uno está inconsciente", 1),
    ("Persona se está ahogando en la piscina, sacamos pero no respira", 1),
    ("Anciano con paro cardíaco, no responde, piel fría, urgente ambulancia", 1),
    ("Trabajador cayó desde altura, mucha sangre, fractura expuesta pierna", 1),
    ("Embarazada con hemorragia abundante, dolor intenso, creo que pierde el bebé", 1),
    ("Quemadura grave por explosión, persona gritando de dolor, piel quemada", 1),
    ("Motociclista en accidente, casco roto, sangre cabeza, inconsciente", 1),
    ("Ataque epiléptico severo, convulsionando hace 10 minutos, no para", 1),

    # PRIORIDAD 2 - URGENTE (Requiere atención inmediata)
    ("Accidente de tráfico, persona herida consciente pero con mucho dolor", 2),
    ("Caída de moto, conductor con heridas en brazo y pierna, sangrado moderado", 2),
    ("Mi madre diabética está muy mareada y confundida, no reconoce dónde está", 2),
    ("Herida profunda con cuchillo en la mano, sangra bastante", 2),
    ("Persona golpeada en asalto, heridas en cara y costillas, dolor al respirar", 2),
    ("Anciano se cayó en el baño, posible fractura de cadera, mucho dolor", 2),
    ("Quemadura con agua caliente en brazo y pecho, piel con ampollas", 2),
    ("Niño con fiebre muy alta 40 grados, convulsionó hace un rato", 2),
    ("Embarazada con contracciones muy seguidas, creo que va a dar a luz pronto", 2),
    ("Accidente laboral, mano atrapada en máquina, dedos lastimados", 2),
    ("Perro mordió a niño en la pierna, herida profunda sangrando", 2),
    ("Dolor abdominal intenso, vómitos constantes, posible apendicitis", 2),
    ("Reacción alérgica severa, hinchazón en cara y garganta, dificultad respirar", 2),
    ("Ciclista atropellado, golpes fuertes, dolor intenso en espalda", 2),
    ("Cortadura profunda al manipular vidrios, sangrado abundante mano", 2),

    # PRIORIDAD 3 - MODERADO (Atención necesaria)
    ("Persona tropezó y se torció el tobillo, hinchado y duele para caminar", 3),
    ("Golpe en la cabeza al caer, tiene chichón pero está consciente", 3),
    ("Cortadura en la mano al cocinar, sangra un poco pero está controlado", 3),
    ("Dolor fuerte en espalda después de cargar cosas pesadas", 3),
    ("Niño con vómitos y diarrea desde ayer, está débil", 3),
    ("Picadura de insecto con hinchazón y dolor, no es alérgico", 3),
    ("Resbaló en la escalera, dolores varios pero puede moverse", 3),
    ("Accidente menor de auto, golpes leves, precaución revisión médica", 3),
    ("Dolor de muelas muy fuerte, hinchazón en la cara", 3),
    ("Caída leve de bicicleta, raspones y moretones, nada grave", 3),
    ("Dolor en el pecho leve, podría ser reflujo o ansiedad", 3),
    ("Anciано con presión arterial un poco alta, mareos leves", 3),
    ("Golpe en rodilla jugando fútbol, hinchado y duele al caminar", 3),
    ("Resfriado fuerte con fiebre de 38, tos y malestar general", 3),
    ("Cortadura pequeña pero necesita puntos para cerrar bien", 3),

    # PRIORIDAD 4 - MENOR (Puede esperar)
    ("Consulta por dolor de garganta que lleva varios días", 4),
    ("Tos persistente hace una semana, sin fiebre", 4),
    ("Erupción en la piel que pica, apareció hace dos días", 4),
    ("Dolor de espalda crónico, necesito evaluación médica", 4),
    ("Moretón grande en la pierna por golpe de hace días", 4),
    ("Consulta por control de diabetes, necesito ajustar medicación", 4),
    ("Raspadura leve al caer caminando, ya desinfecté", 4),
    ("Torcedura de muñeca leve, puedo moverla pero molesta", 4),
    ("Consulta para ver si necesito vacuna contra tétanos", 4),
    ("Dolor de cabeza frecuente, quiero que me revisen", 4),
    ("Hinchazón leve en pie después de caminata larga", 4),
    ("Consulta de control post-operatorio, sin complicaciones", 4),
    ("Picadura de mosquito infectada, un poco roja", 4),
    ("Verrugas que quiero que me retiren", 4),
    ("Dolor articular leve, artritis crónica", 4),

    # PRIORIDAD 5 - NO URGENTE (Atención diferida)
    ("Consulta de rutina para chequeo general", 5),
    ("Necesito receta para medicamento de presión", 5),
    ("Control de embarazo programado", 5),
    ("Solicitud de certificado médico para trabajo", 5),
    ("Consulta nutricional para bajar de peso", 5),
    ("Chequeo preventivo de salud anual", 5),
    ("Consulta dermatológica por lunares, sin cambios", 5),
    ("Renovación de receta de anticonceptivos", 5),
    ("Examen de la vista, creo que necesito lentes", 5),
    ("Consulta por ronquidos nocturnos", 5),
    ("Chequeo odontológico preventivo", 5),
    ("Consulta para vacunación de viaje", 5),
    ("Control de colesterol, último hace 6 meses", 5),
    ("Evaluación para iniciar ejercicio físico", 5),
    ("Consulta psicológica por estrés laboral", 5),
]


def main():
    """Función principal de entrenamiento"""
    print("=" * 60)
    print("ENTRENAMIENTO DEL MODELO K-MEANS PARA ANÁLISIS DE TEXTO")
    print("=" * 60)

    # 1. Inicializar preprocesador y modelo
    print("\n[1/5] Inicializando componentes...")
    preprocessor = TextPreprocessor()
    model = KMeansTextAnalyzer(n_clusters=settings.num_clusters)

    # 2. Preprocesar textos
    print("[2/5] Preprocesando textos...")
    texts, labels = zip(*TRAINING_DATA)
    processed_texts = [preprocessor.preprocess(text, apply_stemming=True) for text in texts]

    print(f"   - Total de muestras: {len(processed_texts)}")
    print(f"   - Distribución de prioridades:")
    for priority in range(1, 6):
        count = labels.count(priority)
        print(f"     Prioridad {priority}: {count} muestras")

    # 3. Entrenar modelo
    print("\n[3/5] Entrenando modelo K-means...")
    metrics = model.train(processed_texts, labels)

    print(f"   OK - Entrenamiento completado")
    print(f"   - Silhouette Score: {metrics['silhouette_score']:.4f}")
    print(f"   - Inertia: {metrics['inertia']:.2f}")
    print(f"   - Clusters creados: {metrics['n_clusters']}")

    # 4. Mostrar mapeo de clusters
    print("\n[4/5] Mapeo de clusters a prioridades:")
    for cluster_id, priority in metrics['cluster_mapping'].items():
        label = metrics['cluster_labels'][cluster_id]
        print(f"   Cluster {cluster_id} -> Prioridad {priority}: {label}")

    # 5. Guardar modelo
    print("\n[5/5] Guardando modelo...")
    model_path = Path(settings.model_path)
    vectorizer_path = Path(settings.vectorizer_path)

    model.save_model(str(model_path), str(vectorizer_path))
    print(f"   OK - Modelo guardado en: {model_path}")
    print(f"   OK - Vectorizador guardado en: {vectorizer_path}")

    # 6. Prueba rápida
    print("\n" + "=" * 60)
    print("PRUEBA DEL MODELO")
    print("=" * 60)

    test_cases = [
        "Persona inconsciente en accidente de tráfico con hemorragia",
        "Caída leve con golpe en rodilla, puede caminar",
        "Consulta de rutina para chequeo médico"
    ]

    for test_text in test_cases:
        processed = preprocessor.preprocess(test_text, apply_stemming=True)
        result = model.predict(processed)

        print(f"\nTexto: {test_text}")
        print(f"  -> Prioridad: {result['prioridad_calculada']}")
        print(f"  -> Tipo: {result['tipo_incidente_predicho']}")
        print(f"  -> Confianza: {result['score_confianza']:.2%}")

    print("\n" + "=" * 60)
    print("ENTRENAMIENTO COMPLETADO EXITOSAMENTE")
    print("=" * 60)
    print("\nAhora puedes iniciar el servidor con:")
    print("  python -m uvicorn app.main:app --reload --port 8001")


if __name__ == "__main__":
    main()
