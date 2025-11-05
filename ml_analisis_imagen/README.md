# ML Análisis de Imágenes

Servicio de Machine Learning para análisis de imágenes de incidentes usando K-means clustering.

## Características

- Análisis de imágenes con OpenCV
- Clustering K-means para clasificación de severidad visual
- Extracción de características (histogramas, texturas, bordes)
- Detección simple de objetos
- Evaluación de calidad de imagen
- Detección de anomalías
- API REST con FastAPI

## Instalación

```bash
python -m venv venv
venv\Scripts\activate  # Windows
pip install -r requirements.txt
```

## Configuración

Copia `.env.example` a `.env`:

```bash
cp .env.example .env
```

## Entrenamiento

```bash
python train_model.py
```

Genera: `trained_models/kmeans_imagen_model.pkl`

## Ejecución

```bash
python -m uvicorn app.main:app --reload --port 8002
```

Documentación: http://localhost:8002/docs

## API Endpoints

### 1. Health Check
```bash
GET /api/ml/salud
```

### 2. Analizar Imagen (por ruta)
```bash
POST /api/ml/analizar-imagen
{
  "imagen_path": "/path/to/image.jpg",
  "multimedia_id": "uuid-123",
  "incidente_id": "uuid-456"
}
```

### 3. Analizar Imagen (upload)
```bash
POST /api/ml/analizar-imagen-upload
Content-Type: multipart/form-data
file: [imagen]
```

## Response Example

```json
{
  "es_imagen_accidente": true,
  "score_veracidad": 0.87,
  "tipo_escena_detectada": "Escena crítica - Alta severidad",
  "nivel_gravedad_visual": 2,
  "elementos_criticos_detectados": {
    "situacion_critica": 0.8,
    "objetos_grandes": 0.7
  },
  "objetos_detectados": {
    "total_objetos": 15,
    "objetos_grandes": 3,
    "objetos_medianos": 5
  },
  "personas_detectadas": 2,
  "vehiculos_detectados": 1,
  "categorias_escena": {
    "accidente": 0.8,
    "emergencia": 0.7
  },
  "score_confianza_escena": 0.85,
  "es_anomalia": false,
  "score_anomalia": 0.15,
  "calidad_imagen": "BUENA",
  "resolucion_imagen": "1920x1080",
  "es_imagen_clara": true,
  "tiempo_procesamiento_ms": 250
}
```

## Integración con Java

```java
RestTemplate restTemplate = new RestTemplate();
String url = "http://localhost:8002/api/ml/analizar-imagen";

AnalizarImagenRequest request = new AnalizarImagenRequest();
request.setImagenPath(multimedia.getUrlArchivo());
request.setMultimediaId(multimedia.getId());

AnalizarImagenResponse response = restTemplate.postForObject(
    url, request, AnalizarImagenResponse.class
);
```

## Niveles de Severidad

| Severidad | Descripción |
|-----------|-------------|
| 1 | Escena crítica - Alta severidad |
| 2 | Escena urgente - Severidad moderada-alta |
| 3 | Escena moderada |
| 4 | Escena menor |
| 5 | Escena no urgente |

## Licencia

MIT
