# 🚑 Microservicio de Recepción de Incidentes - Sistema de Despacho de Ambulancias

Microservicio desarrollado en Spring Boot para la recepción, validación y análisis de incidentes médicos reportados a través de bots de WhatsApp y Telegram utilizando n8n.

## 📋 Tabla de Contenidos

- [Descripción](#descripción)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [API Endpoints](#api-endpoints)
- [Integración con n8n](#integración-con-n8n)
- [Integración con ML](#integración-con-ml)
- [Almacenamiento Multimedia](#almacenamiento-multimedia)
- [Base de Datos](#base-de-datos)

## 📖 Descripción

Este microservicio es el punto de entrada para la recepción de incidentes médicos en el sistema de despacho de ambulancias. Permite:

- ✅ Recibir solicitudes de ambulancia desde bots de WhatsApp/Telegram (vía n8n)
- ✅ Almacenar información del incidente y archivos multimedia (imágenes, audio)
- ✅ Integración con servicios ML para análisis de texto e imágenes con **KMeans no supervisado**
- ✅ Calcular prioridad y veracidad del incidente automáticamente
- ✅ Gestionar estados del incidente (RECIBIDO → ANALIZADO → APROBADO/RECHAZADO)
- ✅ Proporcionar endpoints REST para consulta y gestión

## 🏗️ Arquitectura

### Arquitectura General del Sistema

```
┌──────────────────┐
│  Solicitante     │
│  (WhatsApp/      │
│   Telegram)      │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐     ┌────────────────────┐
│      n8n         │────▶│  ms_recepcion      │
│  (Automatización)│     │  (Spring Boot)     │
└──────────────────┘     └─────────┬──────────┘
                                   │
                    ┏──────────────┼──────────────┓
                    ▼              ▼              ▼
            ┌──────────────┐  ┌──────────┐  ┌──────────┐
            │  PostgreSQL  │  │ Servicio │  │   File   │
            │   + JSONB    │  │    ML    │  │  System  │
            └──────────────┘  │ (KMeans) │  │ (uploads)│
                              └──────────┘  └──────────┘
```

### Arquitectura del Microservicio (3 Capas)

```
┌─────────────────────────────────────────┐
│          Controllers (REST)             │
│  - IncidenteController                  │
│  - MultimediaController                 │
│  - SolicitanteController                │
│  - AnalisisController                   │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│            Services                     │
│  - IncidenteService                     │
│  - MultimediaService                    │
│  - AnalisisMlService                    │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│         Repositories (JPA)              │
│  - IncidenteRepository                  │
│  - MultimediaRepository                 │
│  - AnalisisRepository                   │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│         PostgreSQL Database             │
│  - incidente                            │
│  - multimedia                           │
│  - analisis_ml_texto                    │
│  - analisis_ml_imagen                   │
└─────────────────────────────────────────┘
```

## 🛠️ Tecnologías

| Componente | Tecnología | Versión |
|------------|------------|---------|
| Framework | Spring Boot | 3.5.7 |
| Lenguaje | Java | 17 |
| Base de Datos | PostgreSQL | 12+ |
| ORM | Spring Data JPA | - |
| Mapeo DTO | MapStruct | 1.5.5 |
| Validación | Jakarta Validation | - |
| Documentación | Swagger/OpenAPI | (pendiente) |
| Build Tool | Maven | 3.8+ |

### Dependencias Clave

- **Hypersistence Utils** - Soporte JSONB para PostgreSQL
- **Lombok** - Reducción de código boilerplate
- **SLF4J** - Logging
- **Spring Web** - REST Controllers

## 📦 Requisitos

### Software Necesario

- ☕ **JDK 17** o superior
- 🗄️ **PostgreSQL 12+** con extensión JSONB
- 📦 **Maven 3.8+**
- 🐍 **Python 3.9+** (para servicio ML)
- 🔄 **n8n** (para automatización de bots)

### Opcional

- 🐳 Docker & Docker Compose
- 📊 Postman o similar (para testing)

## 🚀 Instalación

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd ms_recepcion/recepcion
```

### 2. Configurar Base de Datos

#### Opción A: Crear BD manualmente

```bash
# Conectar a PostgreSQL
psql -U postgres

# Crear base de datos
CREATE DATABASE MSrecepcion;

# Salir
\q

# Ejecutar script de esquema
psql -U postgres -d MSrecepcion -f src/main/resources/db/schema.sql
```

#### Opción B: El esquema ya está creado
Si ya ejecutaste el script SQL, pasa al siguiente paso.

### 3. Instalar Dependencias

```bash
./mvnw clean install -DskipTests
```

## ⚙️ Configuración

### Archivo `application.yml`

Ubicación: `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/MSrecepcion
    username: postgres
    password: 123456  # Cambiar en producción

  jpa:
    hibernate:
      ddl-auto: validate

app:
  multimedia:
    upload-dir: ./uploads
    max-file-size: 10485760  # 10MB

server:
  port: 8080
```

### Variables de Entorno (Recomendado)

Crea un archivo `.env` (no versionarlo):

```bash
# Base de datos
DB_URL=jdbc:postgresql://localhost:5432/MSrecepcion
DB_USERNAME=postgres
DB_PASSWORD=tu_password_seguro

# Multimedia
UPLOAD_DIR=./uploads
BASE_URL=http://localhost:8080/api

# Servidor
SERVER_PORT=8080
```

## ▶️ Ejecución

### Desarrollo (Maven)

```bash
./mvnw spring-boot:run
```

### Producción (JAR)

```bash
# Compilar
./mvnw clean package -DskipTests

# Ejecutar
java -jar target/recepcion-0.0.1-SNAPSHOT.jar
```

### Verificar que está funcionando

```bash
curl http://localhost:8080/api/actuator/health
```

## 📡 API Endpoints

Base URL: `http://localhost:8080/api`

### Incidentes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/incidentes` | Crear incidente |
| GET | `/incidentes/{id}` | Obtener incidente |
| GET | `/incidentes/{id}/detalle` | Obtener con relaciones |
| GET | `/incidentes` | Listar todos (paginado) |
| PUT | `/incidentes/{id}` | Actualizar incidente |
| DELETE | `/incidentes/{id}` | Eliminar incidente |
| PATCH | `/incidentes/{id}/estado` | Cambiar estado |
| POST | `/incidentes/{id}/aprobar` | Aprobar incidente |
| POST | `/incidentes/{id}/rechazar` | Rechazar incidente |
| POST | `/incidentes/{id}/cancelar` | Cancelar incidente |
| GET | `/incidentes/despacho` | Listos para despacho |
| GET | `/incidentes/pendientes-analisis` | Pendientes de ML |
| GET | `/incidentes/prioridad-alta` | Prioridad 1-2 |

### Multimedia

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/multimedia/incidente/{id}/subir` | Subir archivo |
| GET | `/multimedia/{id}` | Obtener metadata |
| GET | `/multimedia/{id}/ver` | Ver archivo (inline) 🔥 |
| GET | `/multimedia/{id}/descargar` | Descargar archivo |
| DELETE | `/multimedia/{id}` | Eliminar archivo |
| GET | `/multimedia/incidente/{id}` | Listar por incidente |
| GET | `/multimedia/pendientes-analisis` | Pendientes de ML |
| PATCH | `/multimedia/{id}/marcar-analizado` | Marcar analizado |

### Solicitantes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/solicitantes` | Crear solicitante |
| GET | `/solicitantes/{id}` | Obtener por ID |
| GET | `/solicitantes/telefono/{telefono}` | Obtener por teléfono |
| GET | `/solicitantes` | Listar todos |
| PUT | `/solicitantes/{id}` | Actualizar |
| DELETE | `/solicitantes/{id}` | Eliminar |

### Análisis ML

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/analisis-texto/{id}` | Obtener análisis texto |
| GET | `/analisis-texto/incidente/{id}` | Por incidente |
| GET | `/analisis-imagen/{id}` | Obtener análisis imagen |
| GET | `/analisis-imagen/incidente/{id}` | Por incidente |

**Ver más endpoints:** Total de **64 endpoints REST** documentados.

## 🤖 Integración con n8n

### Flujo Completo

1. **Bot recibe mensaje** (WhatsApp/Telegram)
2. **n8n extrae información** (texto, ubicación, imagen)
3. **n8n crea solicitante** → `POST /solicitantes`
4. **n8n crea incidente** → `POST /incidentes`
5. **n8n sube imagen** → `POST /multimedia/incidente/{id}/subir`
6. **n8n notifica a ML** → Envía URL de imagen a servicio ML
7. **ML analiza** → Obtiene imagen desde `GET /multimedia/{id}/ver`
8. **ML guarda resultados** → Actualiza análisis en BD
9. **Sistema calcula prioridad final**
10. **Incidente listo para despacho**

### Ejemplo de Workflow en n8n

```json
{
  "nodes": [
    {
      "name": "Telegram Trigger",
      "type": "n8n-nodes-base.telegram",
      "parameters": {
        "updates": ["message"]
      }
    },
    {
      "name": "Crear Incidente",
      "type": "n8n-nodes-base.httpRequest",
      "parameters": {
        "url": "http://localhost:8080/api/incidentes",
        "method": "POST",
        "body": {
          "solicitante": {
            "nombreCompleto": "={{ $json.message.from.first_name }}",
            "telefono": "={{ $json.message.from.id }}",
            "canalOrigen": "TELEGRAM"
          },
          "ubicacion": {
            "descripcionTextual": "={{ $json.message.text }}"
          },
          "descripcionOriginal": "={{ $json.message.text }}"
        }
      }
    },
    {
      "name": "Subir Imagen",
      "type": "n8n-nodes-base.httpRequest",
      "parameters": {
        "url": "=http://localhost:8080/api/multimedia/incidente/{{ $json.id }}/subir",
        "method": "POST",
        "contentType": "multipart-form-data",
        "bodyParameters": {
          "parameters": [
            {
              "name": "archivo",
              "value": "={{ $binary.data }}"
            }
          ]
        }
      }
    }
  ]
}
```

Ver más detalles en: [MULTIMEDIA_STORAGE.md](./MULTIMEDIA_STORAGE.md)

## 🧠 Integración con ML (KMeans No Supervisado)

### Análisis de Texto

El servicio ML utiliza **KMeans + TF-IDF** para:
- 🎯 Clustering de descripciones de incidentes
- 📊 Asignación automática de prioridad (1-5)
- 🔍 Detección de palabras clave críticas
- 🏥 Identificación de entidades médicas

### Análisis de Imagen

El servicio ML utiliza **KMeans + Computer Vision** para:
- ✅ Verificación de veracidad de imágenes
- 🚨 Cálculo de nivel de gravedad visual
- 🔍 Detección de objetos (ambulancias, personas, vehículos)
- ⚠️ Detección de anomalías (imágenes manipuladas)

### Endpoint para Servicio ML

**Obtener multimedia pendiente:**
```bash
GET http://localhost:8080/api/multimedia/pendientes-analisis
```

**Acceder a imagen:**
```bash
GET http://localhost:8080/api/multimedia/{id}/ver
```

Esta URL puede ser consumida directamente por el script Python de ML.

Ver ejemplos de código Python en: [MULTIMEDIA_STORAGE.md](./MULTIMEDIA_STORAGE.md#🧠-integración-con-servicio-ml-kmeans)

## 📁 Almacenamiento Multimedia

### Ubicación por Defecto

```
ms_recepcion/
└── uploads/
    ├── a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
    ├── b2c3d4e5-f6a7-8901-bcde-f12345678901.png
    └── ...
```

### Tipos de Archivo Soportados

- **Imágenes:** JPG, PNG, HEIC, WEBP
- **Audio:** MP3, WAV, OGG (preparado)
- **Video:** MP4, AVI, MOV, WEBM (preparado)

### Límites

- Tamaño máximo por archivo: **10MB**
- Tamaño máximo de request: **15MB**

**Documentación completa:** [MULTIMEDIA_STORAGE.md](./MULTIMEDIA_STORAGE.md)

## 🗄️ Base de Datos

### Esquema Principal

- `solicitante` - Usuarios que reportan incidentes
- `ubicacion` - Ubicaciones geográficas
- `incidente` - Incidentes reportados
- `multimedia` - Archivos multimedia
- `analisis_ml_texto` - Resultados de análisis de texto
- `analisis_ml_imagen` - Resultados de análisis de imágenes
- `incidente_historial_estados` - Auditoría de cambios de estado

### Tipos JSONB

Las siguientes columnas usan JSONB de PostgreSQL para almacenar datos estructurados:

- `analisis_ml_texto.categorias_detectadas`
- `analisis_ml_texto.palabras_clave_criticas`
- `analisis_ml_imagen.objetos_detectados`
- `analisis_ml_imagen.elementos_criticos_detectados`
- `incidente_historial_estados.metadata`

### Triggers

**Trigger: `trg_incidente_estado_cambio`**

Registra automáticamente cada cambio de estado en `incidente_historial_estados`.

## 🧪 Pruebas

### Crear Incidente Completo

```bash
# 1. Crear solicitante
curl -X POST http://localhost:8080/api/solicitantes \
  -H "Content-Type: application/json" \
  -d '{
    "nombreCompleto": "Juan Pérez",
    "telefono": "+59177123456",
    "canalOrigen": "WHATSAPP"
  }'

# Respuesta: { "id": "sol-uuid-123" }

# 2. Crear ubicación
curl -X POST http://localhost:8080/api/ubicaciones \
  -H "Content-Type: application/json" \
  -d '{
    "descripcionTextual": "Av. Cristo Redentor y 4to Anillo",
    "latitud": -17.783,
    "longitud": -63.182,
    "ciudad": "Santa Cruz de la Sierra",
    "distrito": "Zona Norte"
  }'

# Respuesta: { "id": "ubi-uuid-456" }

# 3. Crear incidente
curl -X POST http://localhost:8080/api/incidentes \
  -H "Content-Type: application/json" \
  -d '{
    "solicitante": {
      "nombreCompleto": "Juan Pérez",
      "telefono": "+59177123456",
      "canalOrigen": "WHATSAPP"
    },
    "ubicacion": {
      "descripcionTextual": "Av. Cristo Redentor y 4to Anillo",
      "latitud": -17.783,
      "longitud": -63.182
    },
    "descripcionOriginal": "Accidente de tránsito, persona herida con sangrado",
    "tipoIncidenteReportado": "ACCIDENTE_TRAFICO"
  }'

# Respuesta: { "id": "inc-uuid-789", "estado": "RECIBIDO" }

# 4. Subir imagen
curl -X POST "http://localhost:8080/api/multimedia/incidente/inc-uuid-789/subir" \
  -F "archivo=@./test-images/accidente.jpg" \
  -F "descripcion=Foto del accidente" \
  -F "esPrincipal=true"

# Respuesta: { "id": "mul-uuid-101", "urlArchivo": "./uploads/..." }

# 5. Ver imagen (accesible desde navegador o ML)
# http://localhost:8080/api/multimedia/mul-uuid-101/ver
```

## 🐳 Docker (Preparación)

```yaml
# docker-compose.yml (ejemplo)
version: '3.8'

services:
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: MSrecepcion
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./recepcion/src/main/resources/db/schema.sql:/docker-entrypoint-initdb.d/schema.sql

  ms-recepcion:
    build: ./recepcion
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/MSrecepcion
      UPLOAD_DIR: /app/uploads
    volumes:
      - multimedia-storage:/app/uploads
    depends_on:
      - postgres

volumes:
  postgres-data:
  multimedia-storage:
```

## 📚 Recursos Adicionales

- [MULTIMEDIA_STORAGE.md](./MULTIMEDIA_STORAGE.md) - Guía completa de almacenamiento
- [application.yml](./recepcion/src/main/resources/application.yml) - Configuración
- [schema.sql](./recepcion/src/main/resources/db/schema.sql) - Esquema de base de datos

## 🤝 Contribución

Este es un proyecto académico de Software II.

## 📄 Licencia

Proyecto educativo - Universidad [Nombre].

---

**Desarrollado con ❤️ para el sistema de despacho de ambulancias**
