# ML Análisis de Texto

Servicio de Machine Learning para análisis de texto de incidentes usando K-means clustering.

## Características

- Análisis de texto en español con NLP
- Clustering K-means para clasificación de urgencia (5 niveles de prioridad)
- Extracción de palabras clave críticas
- Identificación de entidades médicas
- Cálculo de score de confianza
- API REST con FastAPI

## Requisitos

- Python 3.10+
- PostgreSQL (opcional, para datos históricos)

## Instalación

### 1. Crear entorno virtual

```bash
python -m venv venv

# Windows
venv\Scripts\activate

# Linux/Mac
source venv/bin/activate
```

### 2. Instalar dependencias

```bash
pip install -r requirements.txt
```

### 3. Descargar recursos de NLTK

El sistema descargará automáticamente los recursos necesarios la primera vez que se ejecute.
O puedes descargarlos manualmente:

```python
import nltk
nltk.download('punkt')
nltk.download('stopwords')
```

### 4. Configurar variables de entorno

Copia `.env.example` a `.env` y ajusta los valores:

```bash
cp .env.example .env
```

## Entrenamiento del Modelo

Antes de usar el servicio, debes entrenar el modelo:

```bash
python train_model.py
```

Esto generará:
- `trained_models/kmeans_texto_model.pkl` - Modelo K-means entrenado
- `trained_models/tfidf_vectorizer.pkl` - Vectorizador TF-IDF

## Ejecución

### Iniciar el servidor

```bash
# Modo desarrollo
python -m uvicorn app.main:app --reload --port 8001

# Modo producción
python -m uvicorn app.main:app --host 0.0.0.0 --port 8001 --workers 4
```

### Acceder a la documentación

- Swagger UI: http://localhost:8001/docs
- ReDoc: http://localhost:8001/redoc

## Uso de la API

### 1. Health Check

```bash
curl -X GET http://localhost:8001/api/ml/salud
```

Response:
```json
{
  "status": "healthy",
  "model_loaded": true,
  "model_version": "kmeans-v1.0",
  "timestamp": "2025-01-10T10:30:00"
}
```

### 2. Analizar Texto

```bash
curl -X POST http://localhost:8001/api/ml/analizar-texto \
  -H "Content-Type: application/json" \
  -d '{
    "texto": "Accidente de tráfico grave, persona inconsciente con hemorragia"
  }'
```

Response:
```json
{
  "prioridad_calculada": 1,
  "nivel_gravedad": 5,
  "tipo_incidente_predicho": "Crítico - Riesgo vital inmediato",
  "categorias_detectadas": {
    "trauma": 0.95,
    "urgencia_vital": 0.89,
    "accidente_transito": 0.92
  },
  "palabras_clave_criticas": [
    "accidente",
    "grave",
    "inconsciente",
    "hemorragia"
  ],
  "entidades_medicas": {
    "sintomas": ["hemorragia", "inconsciente"],
    "vehiculos": ["transito"]
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
```

### 3. Obtener Info del Modelo

```bash
curl -X GET http://localhost:8001/api/ml/modelo/info
```

## Integración con Microservicio Java

El microservicio Java debe llamar a este servicio vía HTTP:

```java
// Ejemplo con RestTemplate
RestTemplate restTemplate = new RestTemplate();
String url = "http://localhost:8001/api/ml/analizar-texto";

AnalizarTextoRequest request = new AnalizarTextoRequest();
request.setTexto(incidente.getDescripcionOriginal());

AnalizarTextoResponse response = restTemplate.postForObject(
    url,
    request,
    AnalizarTextoResponse.class
);

// Guardar resultado en BD
analisisMlTexto.setPrioridadCalculada(response.getPrioridadCalculada());
analisisMlTexto.setScoreConfianza(response.getScoreConfianza());
// ... etc
```

## Arquitectura

```
ml_analisis_texto/
├── app/
│   ├── models/          # Modelo K-means
│   ├── services/        # Lógica de negocio
│   ├── schemas/         # DTOs con Pydantic
│   ├── utils/           # Preprocesamiento de texto
│   ├── config.py        # Configuración
│   └── main.py          # FastAPI app
├── trained_models/      # Modelos serializados
├── data/               # Datos de entrenamiento
├── train_model.py      # Script de entrenamiento
├── requirements.txt    # Dependencias
└── README.md          # Este archivo
```

## Niveles de Prioridad

| Prioridad | Descripción | Ejemplos |
|-----------|-------------|----------|
| 1 | Crítico - Riesgo vital | Inconsciente, paro cardíaco, hemorragia severa |
| 2 | Urgente - Atención inmediata | Fracturas graves, dolor intenso, quemaduras |
| 3 | Moderado - Atención necesaria | Esguinces, cortes, golpes leves |
| 4 | Menor - Puede esperar | Consultas, dolores crónicos |
| 5 | No urgente - Atención diferida | Chequeos rutinarios, certificados |

## Palabras Clave Críticas

El sistema identifica automáticamente:
- Condiciones críticas: inconsciente, paro, hemorragia
- Urgencias vitales: no respira, atrapado, convulsión
- Trauma: accidente, choque, fractura
- Condiciones especiales: embarazada, diabético, alérgico

## Testing

```bash
pytest
```

## Docker (Opcional)

```dockerfile
FROM python:3.10-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]
```

Build y run:
```bash
docker build -t ml-analisis-texto .
docker run -p 8001:8001 ml-analisis-texto
```

## Licencia

MIT
