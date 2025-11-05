# Guía de Inicio Rápido - Sistema ML para Análisis de Incidentes

## Resumen

Sistema completo de Machine Learning para análisis automático de incidentes de ambulancias:
- **ml_analisis_texto**: Analiza descripciones de texto (Python + FastAPI + K-means)
- **ml_analisis_imagen**: Analiza imágenes de incidentes (Python + FastAPI + K-means + OpenCV)
- **recepcion**: Microservicio principal (Java + Spring Boot)

## Inicio Rápido (5 pasos)

### Paso 1: Preparar Servicio de Análisis de Texto

```bash
# 1. Ir a carpeta
cd ml_analisis_texto

# 2. Crear entorno virtual
python -m venv venv

# 3. Activar (Windows)
venv\Scripts\activate

# 4. Instalar dependencias
pip install -r requirements.txt

# 5. Entrenar modelo (genera datos de ejemplo)
python train_model.py
```

**Output esperado:**
```
✓ Entrenamiento completado exitosamente
Modelo guardado en: ./trained_models/kmeans_texto_model.pkl
```

### Paso 2: Iniciar Servicio de Texto

```bash
# Desde ml_analisis_texto/ con venv activado
python -m uvicorn app.main:app --reload --port 8001
```

**Verificar:** http://localhost:8001/docs

### Paso 3: Preparar Servicio de Análisis de Imágenes

```bash
# Nueva terminal
cd ml_analisis_imagen

# Crear y activar venv
python -m venv venv
venv\Scripts\activate

# Instalar dependencias
pip install -r requirements.txt

# Entrenar modelo
python train_model.py
```

### Paso 4: Iniciar Servicio de Imágenes

```bash
# Desde ml_analisis_imagen/ con venv activado
python -m uvicorn app.main:app --reload --port 8002
```

**Verificar:** http://localhost:8002/docs

### Paso 5: Iniciar Microservicio Java

```bash
# Nueva terminal
cd recepcion

# Compilar y ejecutar
./mvnw spring-boot:run
```

**Verificar:** http://localhost:8080/api/swagger-ui.html

## Verificación del Sistema

### 1. Health Checks

```bash
# Servicio ML Texto
curl http://localhost:8001/api/ml/salud

# Servicio ML Imagen
curl http://localhost:8002/api/ml/salud

# Microservicio Java
curl http://localhost:8080/api/actuator/health
```

### 2. Prueba de Análisis de Texto

```bash
curl -X POST http://localhost:8001/api/ml/analizar-texto \
  -H "Content-Type: application/json" \
  -d '{
    "texto": "Accidente de tráfico grave, persona inconsciente con hemorragia"
  }'
```

**Respuesta esperada:**
```json
{
  "prioridad_calculada": 1,
  "nivel_gravedad": 5,
  "tipo_incidente_predicho": "Crítico - Riesgo vital inmediato",
  "palabras_clave_criticas": ["accidente", "grave", "inconsciente", "hemorragia"],
  "score_confianza": 0.92
}
```

### 3. Crear Incidente Completo (desde Java)

```bash
curl -X POST http://localhost:8080/api/incidentes \
  -H "Content-Type: application/json" \
  -d '{
    "solicitante": {
      "nombre": "Juan Pérez",
      "telefono": "+58412123456",
      "email": "juan@example.com"
    },
    "ubicacion": {
      "latitud": 10.4806,
      "longitud": -66.9036,
      "direccion": "Av. Principal, Caracas"
    },
    "descripcionOriginal": "Accidente de tráfico grave, persona inconsciente con hemorragia",
    "tipoIncidenteReportado": "ACCIDENTE"
  }'
```

## Arquitectura del Sistema

```
Puerto 8001: ML Análisis Texto (Python/FastAPI)
   │
   │  HTTP REST
   │
Puerto 8080: Microservicio Java (Spring Boot) ─────── PostgreSQL
   │
   │  HTTP REST
   │
Puerto 8002: ML Análisis Imagen (Python/FastAPI)
```

## Estructura de Carpetas

```
ms_recepcion/
│
├── recepcion/                    # Microservicio Spring Boot
│   ├── src/main/java/...
│   │   ├── client/              # MlTextoClient, MlImagenClient
│   │   ├── dto/ml/              # DTOs para comunicación con ML
│   │   └── ...
│   └── src/main/resources/
│       └── application.yml      # Configuración con URLs de ML
│
├── ml_analisis_texto/           # Servicio ML Python para texto
│   ├── app/
│   │   ├── models/              # K-means model
│   │   ├── services/            # Lógica de análisis
│   │   ├── utils/               # Preprocesamiento NLP
│   │   └── main.py              # FastAPI app
│   ├── trained_models/          # Modelos serializados
│   ├── train_model.py           # Script de entrenamiento
│   └── requirements.txt
│
├── ml_analisis_imagen/          # Servicio ML Python para imágenes
│   ├── app/
│   │   ├── models/              # K-means model
│   │   ├── services/            # Lógica de análisis
│   │   ├── utils/               # Preprocesamiento OpenCV
│   │   └── main.py              # FastAPI app
│   ├── trained_models/          # Modelos serializados
│   ├── train_model.py           # Script de entrenamiento
│   └── requirements.txt
│
└── README_ML_INTEGRATION.md     # Documentación completa
```

## Flujo de Análisis

```
1. Usuario crea incidente con descripción
        ↓
2. Java llama a ML Texto (puerto 8001)
        ↓
3. K-means analiza texto → Prioridad 1-5
        ↓
4. Java guarda análisis en BD
        ↓
5. Si hay imagen, Java llama ML Imagen (puerto 8002)
        ↓
6. K-means analiza imagen → Severidad 1-5
        ↓
7. Java calcula prioridad final (60% texto + 40% imagen)
        ↓
8. Incidente listo para despacho
```

## Niveles de Prioridad

| Prioridad | Descripción | Ejemplos |
|-----------|-------------|----------|
| **1** | Crítico | Inconsciente, paro cardíaco, hemorragia |
| **2** | Urgente | Fractura grave, quemadura severa |
| **3** | Moderado | Esguince, corte profundo |
| **4** | Menor | Golpe leve, dolor moderado |
| **5** | No urgente | Consulta, chequeo |

## Tecnologías Utilizadas

### Servicio ML Texto
- Python 3.10+
- FastAPI (API REST)
- scikit-learn (K-means clustering)
- NLTK (procesamiento de lenguaje natural)
- TF-IDF (vectorización de texto)

### Servicio ML Imagen
- Python 3.10+
- FastAPI (API REST)
- scikit-learn (K-means clustering)
- OpenCV (procesamiento de imágenes)
- NumPy (álgebra lineal)

### Microservicio Java
- Java 17
- Spring Boot 3.x
- PostgreSQL
- RestTemplate (cliente HTTP)

## Datos de Entrenamiento

### Texto
- **Datos actuales:** 75 ejemplos sintéticos (15 por prioridad)
- **Recomendado:** 500-1000 incidentes reales etiquetados
- **Formato:** Descripción + Prioridad (1-5)

### Imagen
- **Datos actuales:** 500 features sintéticos (100 por severidad)
- **Recomendado:** 1000-2000 imágenes reales de incidentes
- **Formato:** Imagen + Severidad (1-5)

## Próximos Pasos

1. **Recopilar datos reales** de incidentes históricos
2. **Reentrenar modelos** con datos reales
3. **Implementar trigger automático** al crear incidente
4. **Agregar YOLO** para mejor detección de objetos en imágenes
5. **Implementar BERT** para mejor análisis de texto
6. **Dockerizar** servicios ML
7. **Implementar cola de mensajes** (RabbitMQ) para análisis asíncrono

## Troubleshooting Común

### Error: "Modelo no entrenado"
**Solución:** Ejecutar `python train_model.py` primero

### Error: "Connection refused"
**Solución:** Verificar que los 3 servicios estén corriendo

### Error: ImportError en Python
**Solución:**
```bash
# Activar venv
venv\Scripts\activate

# Reinstalar dependencias
pip install -r requirements.txt
```

### Error: Puerto ocupado
**Solución:**
```bash
# Windows: Matar proceso en puerto
netstat -ano | findstr :8001
taskkill /PID [PID] /F
```

## Comandos Útiles

```bash
# Ver logs en tiempo real (Java)
tail -f recepcion/logs/application.log

# Verificar servicios Python activos
netstat -an | findstr 8001
netstat -an | findstr 8002

# Reiniciar todos los servicios
# Ctrl+C en cada terminal, luego reiniciar
```

## Documentación Completa

- **README_ML_INTEGRATION.md**: Arquitectura completa y detalles técnicos
- **ml_analisis_texto/README.md**: Documentación del servicio de texto
- **ml_analisis_imagen/README.md**: Documentación del servicio de imágenes

## Contacto

Para dudas o soporte, contactar al equipo de desarrollo.

---

**¡Sistema listo para usar!** 🚀
