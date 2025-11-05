# Sistema de Machine Learning para Análisis de Incidentes

Sistema completo de análisis automático de incidentes usando Machine Learning con K-means para el microservicio de recepción de ambulancias.

## Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                   MICROSERVICIO JAVA                         │
│                    (Puerto 8080)                             │
│                                                              │
│  ┌────────────────┐      ┌────────────────┐                │
│  │   Incidente    │──────│  MlTextoClient │──────┐         │
│  │   Controller   │      └────────────────┘       │         │
│  └────────────────┘                               │         │
│          │                                        │         │
│          │                 ┌────────────────┐    │         │
│          └─────────────────│ MlImagenClient │────┼─────┐   │
│                            └────────────────┘    │     │   │
└────────────────────────────────────────────────┬─┴─────┼───┘
                                                  │       │
                                   HTTP REST API │       │
                           ┌───────────────────── ┘       └──────────────┐
                           │                                             │
              ┌────────────▼───────────┐              ┌─────────────────▼────────┐
              │  SERVICIO ML TEXTO     │              │  SERVICIO ML IMAGEN      │
              │    (Puerto 8001)       │              │    (Puerto 8002)         │
              │                        │              │                          │
              │  ┌──────────────────┐  │              │  ┌───────────────────┐  │
              │  │   K-means Text   │  │              │  │  K-means Image    │  │
              │  │   Clustering     │  │              │  │   Clustering      │  │
              │  │   (5 clusters)   │  │              │  │   (5 clusters)    │  │
              │  └──────────────────┘  │              │  └───────────────────┘  │
              │  ┌──────────────────┐  │              │  ┌───────────────────┐  │
              │  │   NLP Pipeline   │  │              │  │ Image Processing  │  │
              │  │  - Tokenización  │  │              │  │  - Extracción     │  │
              │  │  - TF-IDF        │  │              │  │  - Histogramas    │  │
              │  │  - Stemming      │  │              │  │  - Detección obj  │  │
              │  └──────────────────┘  │              │  └───────────────────┘  │
              └────────────────────────┘              └──────────────────────────┘
                  Python + FastAPI                         Python + FastAPI
```

## Componentes del Sistema

### 1. Servicio ML de Análisis de Texto (`ml_analisis_texto/`)

**Tecnologías:**
- Python 3.10+
- FastAPI
- scikit-learn (K-means)
- NLTK (NLP)
- TF-IDF vectorización

**Funcionalidades:**
- Análisis de texto en español
- Clustering K-means (5 niveles de prioridad)
- Extracción de palabras clave críticas
- Identificación de entidades médicas
- Cálculo de confianza

**API Endpoints:**
- `POST /api/ml/analizar-texto` - Analizar descripción
- `GET /api/ml/salud` - Health check
- `GET /api/ml/modelo/info` - Info del modelo

### 2. Servicio ML de Análisis de Imágenes (`ml_analisis_imagen/`)

**Tecnologías:**
- Python 3.10+
- FastAPI
- scikit-learn (K-means)
- OpenCV
- Extracción de features visuales

**Funcionalidades:**
- Análisis de imágenes
- Clustering K-means (5 niveles de severidad)
- Detección simple de objetos
- Evaluación de calidad de imagen
- Detección de anomalías
- Score de veracidad

**API Endpoints:**
- `POST /api/ml/analizar-imagen` - Analizar imagen por ruta
- `POST /api/ml/analizar-imagen-upload` - Upload y analizar
- `GET /api/ml/salud` - Health check

### 3. Microservicio Java (`recepcion/`)

**Integración:**
- `MlTextoClient` - Cliente HTTP para servicio de texto
- `MlImagenClient` - Cliente HTTP para servicio de imagen
- RestTemplate con timeouts configurados
- Manejo de errores y fallbacks

## Instalación y Configuración

### Paso 1: Instalar Servicio de Análisis de Texto

```bash
cd ml_analisis_texto

# Crear entorno virtual
python -m venv venv
venv\Scripts\activate  # Windows
# source venv/bin/activate  # Linux/Mac

# Instalar dependencias
pip install -r requirements.txt

# Configurar environment
cp .env.example .env
# Editar .env con tus configuraciones

# Entrenar modelo
python train_model.py

# Iniciar servidor
python -m uvicorn app.main:app --reload --port 8001
```

### Paso 2: Instalar Servicio de Análisis de Imágenes

```bash
cd ml_analisis_imagen

# Crear entorno virtual
python -m venv venv
venv\Scripts\activate  # Windows

# Instalar dependencias
pip install -r requirements.txt

# Configurar environment
cp .env.example .env

# Entrenar modelo
python train_model.py

# Iniciar servidor
python -m uvicorn app.main:app --reload --port 8002
```

### Paso 3: Configurar Microservicio Java

El microservicio Java ya está configurado con:
- `MlTextoClient` y `MlImagenClient` implementados
- DTOs para comunicación
- Configuración en `application.yml`

Variables de entorno opcionales:
```bash
export ML_TEXTO_URL=http://localhost:8001
export ML_IMAGEN_URL=http://localhost:8002
```

### Paso 4: Iniciar Sistema Completo

```bash
# Terminal 1: Servicio ML Texto
cd ml_analisis_texto
venv\Scripts\activate
python -m uvicorn app.main:app --reload --port 8001

# Terminal 2: Servicio ML Imagen
cd ml_analisis_imagen
venv\Scripts\activate
python -m uvicorn app.main:app --reload --port 8002

# Terminal 3: Microservicio Java
cd recepcion
./mvnw spring-boot:run
```

## Uso del Sistema

### 1. Crear Incidente con Análisis Automático

```bash
# 1. Crear incidente
POST http://localhost:8080/api/incidentes
{
  "solicitante": {
    "nombre": "Juan Pérez",
    "telefono": "+58412123456"
  },
  "ubicacion": {
    "latitud": 10.4806,
    "longitud": -66.9036,
    "direccion": "Av. Principal, Caracas"
  },
  "descripcionOriginal": "Accidente de tráfico grave, persona inconsciente con hemorragia",
  "tipoIncidenteReportado": "ACCIDENTE"
}

# Response incluye incidenteId
```

### 2. El Sistema Automáticamente:

**a) Analiza el Texto:**
```java
// IncidenteServiceImpl.java
AnalizarTextoResponse analisisTexto = mlTextoClient.analizarTexto(
    incidente.getDescripcionOriginal(),
    incidente.getId()
);

// Guarda resultado en BD
incidente.setPrioridadTexto(analisisTexto.getPrioridadCalculada());
incidente.setTipoIncidenteClasificado(analisisTexto.getTipoIncidentePredicho());
```

**b) Si hay imágenes, las analiza:**
```java
AnalizarImagenResponse analisisImagen = mlImagenClient.analizarImagen(
    multimedia.getUrlArchivo(),
    multimedia.getId(),
    incidente.getId()
);

incidente.setPrioridadImagen(analisisImagen.getNivelGravedadVisual());
incidente.setScoreVeracidad(analisisImagen.getScoreVeracidad());
```

**c) Calcula prioridad final:**
```java
if (prioridadTexto != null && prioridadImagen != null) {
    // Promedio ponderado: 60% texto, 40% imagen
    prioridadFinal = (int) Math.round(prioridadTexto * 0.6 + prioridadImagen * 0.4);
} else if (prioridadTexto != null) {
    prioridadFinal = prioridadTexto;
}
```

### 3. Consultar Resultado

```bash
GET http://localhost:8080/api/incidentes/{id}/detalle

Response:
{
  "id": "uuid-123",
  "prioridadInicial": 3,
  "prioridadTexto": 1,
  "prioridadImagen": 2,
  "prioridadFinal": 1,
  "scoreVeracidad": 0.92,
  "esVerosimil": true,
  "estadoIncidente": "ANALIZADO",
  "analisisTexto": {
    "prioridadCalculada": 1,
    "nivelGravedad": 5,
    "tipoIncidentePredicho": "Crítico - Riesgo vital inmediato",
    "palabrasClaveCriticas": ["accidente", "grave", "inconsciente", "hemorragia"],
    "scoreConfianza": 0.92
  },
  "multimedia": [
    {
      "analisisImagen": {
        "esImagenAccidente": true,
        "scoreVeracidad": 0.87,
        "nivelGravedadVisual": 2,
        "personasDetectadas": 2,
        "vehiculosDetectados": 1
      }
    }
  ]
}
```

## Flujo de Estados

```
INCIDENTE CREADO (estado: RECIBIDO)
    │
    ▼
ANÁLISIS DE TEXTO AUTOMÁTICO
    │
    ├─ Prioridad calculada: 1-5
    ├─ Tipo incidente predicho
    ├─ Palabras clave críticas
    └─ Score de confianza
    │
    ▼
ANÁLISIS DE IMAGEN (si hay multimedia)
    │
    ├─ Severidad visual: 1-5
    ├─ Score de veracidad
    ├─ Detección de objetos
    └─ Anomalías
    │
    ▼
CÁLCULO DE PRIORIDAD FINAL
    │
    └─ Combina análisis texto + imagen
    │
    ▼
ESTADO: ANALIZADO
    │
    ├─ APROBAR → Enviar a despacho
    ├─ RECHAZAR → Falsa alarma
    └─ CANCELAR → Solicitante cancela
```

## Niveles de Prioridad

| Nivel | Descripción | Tiempo Respuesta Estimado |
|-------|-------------|---------------------------|
| **1** | Crítico - Riesgo vital inmediato | < 5 minutos |
| **2** | Urgente - Requiere atención inmediata | < 15 minutos |
| **3** | Moderado - Atención necesaria | < 30 minutos |
| **4** | Menor - Puede esperar | < 1 hora |
| **5** | No urgente - Atención diferida | < 4 horas |

## Palabras Clave Críticas Detectadas

- **Máxima Urgencia:** inconsciente, no respira, paro cardíaco, hemorragia, atrapado
- **Alta Urgencia:** herido, fractura, quemadura, accidente, choque, grave
- **Entidades Médicas:** embarazada, diabético, epiléptico, alérgico
- **Partes del Cuerpo:** cabeza, torax, abdomen, columna
- **Síntomas:** dolor, sangrado, convulsión, desmayo

## Monitoreo y Salud

### Health Checks

```bash
# Servicio ML Texto
GET http://localhost:8001/api/ml/salud

# Servicio ML Imagen
GET http://localhost:8002/api/ml/salud

# Microservicio Java
GET http://localhost:8080/api/actuator/health
```

### Logs

- **ML Texto:** Logs en consola de Python
- **ML Imagen:** Logs en consola de Python
- **Java:** Configurado en `application.yml` (nivel DEBUG)

## Próximos Pasos / Mejoras

### 1. Datos Reales
- [ ] Recopilar dataset real de incidentes históricos
- [ ] Recopilar imágenes reales de incidentes
- [ ] Reentrenar modelos con datos reales

### 2. Modelos Avanzados
- [ ] Implementar BERT para texto (mejora sobre TF-IDF)
- [ ] Implementar YOLO para detección de objetos en imágenes
- [ ] Implementar autoencoder para detección de anomalías

### 3. Automatización
- [ ] Trigger automático de análisis al crear incidente
- [ ] Webhook para notificar cuando análisis completa
- [ ] Cola de mensajes (RabbitMQ/Kafka) para procesar en background

### 4. Producción
- [ ] Dockerizar servicios ML
- [ ] Implementar caché de resultados
- [ ] Métricas y monitoring (Prometheus/Grafana)
- [ ] CI/CD pipeline

## Troubleshooting

### Problema: Modelo no cargado

**Solución:** Entrenar el modelo primero
```bash
python train_model.py
```

### Problema: Servicio ML no disponible

**Solución:** Verificar que los servicios estén corriendo
```bash
# Check si puerto está ocupado
netstat -an | findstr 8001
netstat -an | findstr 8002
```

### Problema: Error de conexión desde Java

**Solución:** Verificar URL en `application.yml`
```yaml
ml:
  texto:
    base-url: http://localhost:8001
  imagen:
    base-url: http://localhost:8002
```

## Contacto y Soporte

Para preguntas o soporte, contactar al equipo de desarrollo.

## Licencia

MIT License
