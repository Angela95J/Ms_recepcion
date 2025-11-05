# 📁 Guía de Almacenamiento de Multimedia

## Descripción General

El microservicio de recepción de incidentes almacena archivos multimedia (imágenes, audio, video) de forma **local** en el sistema de archivos del servidor.

## 🗂️ Configuración del Almacenamiento

### Ubicación de Archivos

Por defecto, los archivos se almacenan en:
```
./uploads/
```

Esta ruta es **relativa al directorio de ejecución** de la aplicación.

### Configuración en `application.yml`

```yaml
app:
  multimedia:
    upload-dir: ./uploads              # Directorio de almacenamiento
    max-file-size: 10485760           # Tamaño máximo: 10MB
    base-url: http://localhost:8080/api
```

### Variables de Entorno (Opcional)

Puedes sobrescribir la configuración usando variables de entorno:

```bash
# Directorio personalizado
export UPLOAD_DIR=/var/multimedia/uploads

# URL base personalizada
export BASE_URL=http://tu-servidor:8080/api
```

## 📤 Subida de Archivos

### Endpoint

```
POST /api/multimedia/incidente/{incidenteId}/subir
Content-Type: multipart/form-data
```

### Parámetros

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `archivo` | File | Sí | Archivo multimedia (imagen) |
| `descripcion` | String | No | Descripción del archivo |
| `esPrincipal` | Boolean | No | Si es la imagen principal (default: false) |

### Ejemplo con cURL

```bash
curl -X POST "http://localhost:8080/api/multimedia/incidente/{incidenteId}/subir" \
  -H "Content-Type: multipart/form-data" \
  -F "archivo=@/path/to/image.jpg" \
  -F "descripcion=Imagen del accidente" \
  -F "esPrincipal=true"
```

### Respuesta Exitosa

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "incidenteId": "12345678-1234-1234-1234-123456789012",
  "urlArchivo": "./uploads/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg",
  "nombreArchivo": "image.jpg",
  "tipoArchivo": "IMAGEN",
  "formatoArchivo": "jpg",
  "tamanoBytes": 2048576,
  "descripcion": "Imagen del accidente",
  "esPrincipal": true,
  "requiereAnalisisMl": true,
  "analisisCompletado": false,
  "fechaSubida": "2025-01-15T10:30:00"
}
```

## 📥 Acceso a Archivos

### 1. Ver/Servir Archivo (Inline) - **Para n8n y ML**

```
GET /api/multimedia/{id}/ver
```

Este endpoint sirve el archivo **inline** (visualización directa en navegador).

**Ideal para:**
- ✅ Integración con n8n
- ✅ Servicio de ML (análisis de imágenes)
- ✅ Visualización en frontend
- ✅ Webhooks y automatizaciones

**Ejemplo:**
```
http://localhost:8080/api/multimedia/a1b2c3d4-e5f6-7890-abcd-ef1234567890/ver
```

**Headers de respuesta:**
```
Content-Type: image/jpeg
Content-Disposition: inline; filename="image.jpg"
Cache-Control: max-age=3600
```

### 2. Descargar Archivo (Attachment)

```
GET /api/multimedia/{id}/descargar
```

Descarga el archivo como attachment (fuerza descarga en navegador).

### 3. Obtener Metadatos

```
GET /api/multimedia/{id}
```

Devuelve información del archivo sin descargarlo.

## 🤖 Integración con n8n

### Flujo Completo: WhatsApp/Telegram → n8n → Microservicio

#### 1. Recibir Imagen desde Bot

**Nodo: Telegram/WhatsApp Trigger**

El bot recibe el mensaje con imagen del solicitante.

```json
{
  "message": {
    "from": { "id": 123456, "username": "usuario" },
    "text": "Necesito ambulancia urgente, accidente de tránsito",
    "photo": [{
      "file_id": "ABC123...",
      "file_url": "https://api.telegram.org/file/bot.../photo.jpg"
    }]
  }
}
```

#### 2. Descargar Imagen del Bot

**Nodo: HTTP Request**

Descargar la imagen desde Telegram/WhatsApp:

```
GET {{ $json.message.photo[0].file_url }}
Response Format: File
```

#### 3. Crear Incidente

**Nodo: HTTP Request**

```
POST http://localhost:8080/api/incidentes
Content-Type: application/json

{
  "solicitante": {
    "nombreCompleto": "{{ $json.message.from.first_name }} {{ $json.message.from.last_name }}",
    "telefono": "{{ $json.message.from.id }}",
    "canalOrigen": "TELEGRAM"
  },
  "ubicacion": {
    "descripcionTextual": "{{ $json.message.location.address }}",
    "latitud": {{ $json.message.location.latitude }},
    "longitud": {{ $json.message.location.longitude }}
  },
  "descripcionOriginal": "{{ $json.message.text }}"
}
```

Respuesta:
```json
{
  "id": "12345678-1234-1234-1234-123456789012",
  ...
}
```

#### 4. Subir Imagen al Microservicio

**Nodo: HTTP Request**

```
POST http://localhost:8080/api/multimedia/incidente/{{$node["Crear Incidente"].json["id"]}}/subir
Content-Type: multipart/form-data

Body (Form-Data):
- archivo: {{ $binary.data }}
- descripcion: "Imagen reportada por usuario"
- esPrincipal: true
```

Respuesta:
```json
{
  "id": "a1b2c3d4-...",
  "urlArchivo": "./uploads/a1b2c3d4-....jpg",
  ...
}
```

#### 5. Enviar a Análisis ML

**Nodo: HTTP Request → Servicio ML**

```
POST http://localhost:5000/ml/analizar-imagen
Content-Type: application/json

{
  "multimedia_id": "{{ $json.id }}",
  "imagen_url": "http://localhost:8080/api/multimedia/{{ $json.id }}/ver",
  "incidente_id": "{{ $node["Crear Incidente"].json["id"] }}"
}
```

## 🧠 Integración con Servicio ML (KMeans)

### Arquitectura

```
┌─────────────┐      ┌──────────────────┐      ┌─────────────┐
│   n8n       │─────▶│  ms_recepcion    │◀─────│  Servicio   │
│  (Webhook)  │      │  (Spring Boot)   │      │     ML      │
└─────────────┘      └──────────────────┘      │  (Python)   │
                              │                 └─────────────┘
                              ▼
                     ┌──────────────────┐
                     │   PostgreSQL     │
                     │   + JSONB        │
                     └──────────────────┘
```

### Script Python para Análisis ML

```python
import requests
from sklearn.cluster import KMeans
from PIL import Image
import numpy as np

# 1. Obtener multimedia pendiente de análisis
response = requests.get('http://localhost:8080/api/multimedia/pendientes-analisis')
multimedia_pendiente = response.json()

for multimedia in multimedia_pendiente:
    multimedia_id = multimedia['id']
    imagen_url = f"http://localhost:8080/api/multimedia/{multimedia_id}/ver"

    # 2. Descargar imagen
    img_response = requests.get(imagen_url)
    img = Image.open(BytesIO(img_response.content))

    # 3. Análisis con KMeans (ejemplo simplificado)
    img_array = np.array(img)
    pixels = img_array.reshape(-1, 3)

    kmeans = KMeans(n_clusters=5, random_state=42)
    kmeans.fit(pixels)

    # 4. Calcular veracidad y gravedad (tu lógica personalizada)
    score_veracidad = calcular_veracidad(img_array, kmeans)
    nivel_gravedad = calcular_gravedad(img_array, kmeans)

    # 5. Guardar resultados en la base de datos
    resultado = {
        "multimediaId": multimedia_id,
        "esImagenAccidente": score_veracidad > 0.7,
        "scoreVeracidad": score_veracidad,
        "nivelGravedadVisual": nivel_gravedad,
        "elementosCriticosDetectados": {
            "sangre": True,
            "vehiculo_dañado": True
        },
        "objetosDetectados": {
            "vehiculos": 2,
            "personas": 3
        },
        "personasDetectadas": 3,
        "vehiculosDetectados": 2,
        "categoriasEscena": {
            "accidente_trafico": 0.95
        },
        "scoreConfianzaEscena": 0.95,
        "esAnomalia": False
    }

    # POST a la API
    requests.post(
        'http://localhost:8080/api/analisis-imagen',
        json=resultado
    )

    # Marcar como analizado
    requests.patch(
        f'http://localhost:8080/api/multimedia/{multimedia_id}/marcar-analizado'
    )
```

### Análisis de Texto con KMeans

```python
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.cluster import KMeans

# 1. Obtener incidentes pendientes de análisis texto
response = requests.get('http://localhost:8080/api/incidentes/pendientes-analisis')
incidentes = response.json()

textos = [inc['descripcionOriginal'] for inc in incidentes]

# 2. Vectorización con TF-IDF
vectorizer = TfidfVectorizer(max_features=100)
X = vectorizer.fit_transform(textos)

# 3. Clustering con KMeans
kmeans = KMeans(n_clusters=5, random_state=42)
clusters = kmeans.fit_predict(X)

# 4. Mapear clusters a prioridades
# Cluster 0 = Emergencia crítica (prioridad 1)
# Cluster 1 = Urgente (prioridad 2)
# ...
cluster_to_priority = {0: 1, 1: 2, 2: 3, 3: 4, 4: 5}

for i, incidente in enumerate(incidentes):
    cluster = clusters[i]
    prioridad = cluster_to_priority[cluster]

    # Guardar análisis
    resultado = {
        "textoAnalizado": incidente['descripcionOriginal'],
        "prioridadCalculada": prioridad,
        "nivelGravedad": prioridad,
        "tipoIncidentePredicho": "ACCIDENTE_TRAFICO",
        "categoriasDetectadas": {"emergencia": True},
        "palabrasClaveCriticas": {"sangre": 1, "accidente": 1},
        "scoreConfianza": 0.85
    }

    requests.post(
        'http://localhost:8080/api/analisis-texto',
        json=resultado
    )
```

## 🔧 Estructura de Directorios Recomendada

```
ms_recepcion/
├── uploads/                    # Archivos multimedia
│   ├── a1b2c3d4-...jpg
│   ├── e5f6g7h8-...png
│   └── ...
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
└── README.md
```

## 🐳 Preparación para Docker

Cuando dockerices la aplicación, considera:

### 1. Volumen para Persistencia

```yaml
# docker-compose.yml
services:
  ms-recepcion:
    image: ms-recepcion:latest
    volumes:
      - ./uploads:/app/uploads  # Mapear directorio local
    environment:
      - UPLOAD_DIR=/app/uploads
```

### 2. Compartir con Servicio ML

```yaml
services:
  ms-recepcion:
    volumes:
      - multimedia-storage:/app/uploads

  ml-service:
    volumes:
      - multimedia-storage:/data/images  # Mismo volumen

volumes:
  multimedia-storage:
```

## 📊 Monitoreo y Limpieza

### Ver Espacio Usado

```bash
du -sh ./uploads
```

### Limpiar Archivos Antiguos (Opcional)

```bash
# Eliminar archivos con más de 30 días
find ./uploads -type f -mtime +30 -delete
```

## ⚠️ Limitaciones del Almacenamiento Local

1. **No escalable horizontalmente** - Si tienes múltiples instancias, cada una tiene sus propios archivos
2. **Sin backup automático** - Debes configurar backups manualmente
3. **Límite de espacio** - Depende del disco del servidor
4. **Sin CDN** - No hay optimización de entrega

**Recomendación:** Para producción, considera migrar a MinIO, S3, o similar.

## 📞 Soporte

Para más información, revisa:
- [README.md](./README.md) - Documentación general del proyecto
- [application.yml](./src/main/resources/application.yml) - Configuración completa
- [MultimediaController.java](./src/main/java/com/recepcion/recepcion/controller/MultimediaController.java) - Endpoints disponibles
