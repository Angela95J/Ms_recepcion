"""
Script para probar el endpoint de análisis de imagen
"""
import requests
import json
from pathlib import Path

# URL del servicio
BASE_URL = "http://127.0.0.1:8002"

def test_health():
    """Prueba el endpoint de salud"""
    print("=" * 60)
    print("TEST 1: Health Check")
    print("=" * 60)

    response = requests.get(f"{BASE_URL}/api/ml/salud")
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
    print()

def test_analyze_image():
    """Prueba el endpoint de análisis de imagen"""
    print("=" * 60)
    print("TEST 2: Analizar Imagen")
    print("=" * 60)

    # Usar imagen de ejemplo de sklearn
    image_path = Path("venv/Lib/site-packages/sklearn/datasets/images/flower.jpg")

    if not image_path.exists():
        print(f"ERROR: No se encontró la imagen en {image_path}")
        return

    print(f"Analizando imagen: {image_path}")

    # Preparar el multipart form data
    with open(image_path, 'rb') as img_file:
        files = {
            'imagen': ('test_image.jpg', img_file, 'image/jpeg')
        }
        data = {
            'multimedia_id': '550e8400-e29b-41d4-a716-446655440000',
            'incidente_id': '550e8400-e29b-41d4-a716-446655440001'
        }

        response = requests.post(
            f"{BASE_URL}/api/ml/analizar-imagen",
            files=files,
            data=data
        )

    print(f"Status: {response.status_code}")

    if response.status_code == 200:
        result = response.json()
        print("\nResultados del análisis:")
        print(f"  - Es imagen de accidente: {result['es_imagen_accidente']}")
        print(f"  - Score veracidad: {result['score_veracidad']}")
        print(f"  - Tipo escena: {result['tipo_escena_detectada']}")
        print(f"  - Nivel gravedad: {result['nivel_gravedad_visual']}")
        print(f"  - Personas detectadas: {result['personas_detectadas']}")
        print(f"  - Vehículos detectados: {result['vehiculos_detectados']}")
        print(f"  - Calidad imagen: {result['calidad_imagen']}")
        print(f"  - Es imagen clara: {result['es_imagen_clara']}")
        print(f"  - Tiempo procesamiento: {result['tiempo_procesamiento_ms']} ms")
        print(f"  - Objetos detectados: {result['objetos_detectados']}")
        print(f"  - Elementos críticos: {result['elementos_criticos_detectados']}")
    else:
        print(f"ERROR: {response.text}")

    print()

if __name__ == "__main__":
    try:
        test_health()
        test_analyze_image()

        print("=" * 60)
        print("PRUEBAS COMPLETADAS")
        print("=" * 60)

    except requests.exceptions.ConnectionError:
        print("ERROR: No se puede conectar al servicio ML de imagen")
        print("Asegurate de que el servicio este corriendo en puerto 8002")
    except Exception as e:
        print(f"ERROR: {e}")
