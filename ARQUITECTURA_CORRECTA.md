# Arquitectura Correcta: n8n → Controladores Existentes

## Resumen

Este documento describe la arquitectura correcta para la integración entre el bot de Telegram (n8n) y el microservicio de recepción. La solución elimina la capa innecesaria de `ConversacionBot` y aprovecha los controladores existentes del microservicio.

---

## Problema Identificado

Se creó una capa adicional (`ConversacionBotController`, `ConversacionBotService`, etc.) que duplicaba funcionalidad y generaba complejidad innecesaria. El microservicio **YA TENÍA** todos los endpoints necesarios.

---

## Solución Implementada

### 1. Eliminación de Código Innecesario

Se eliminaron los siguientes archivos:
- ❌ `ConversacionBotController.java`
- ❌ `ConversacionBotService.java`
- ❌ `ConversacionBotRepository.java`
- ❌ `ConversacionBot.java` (entidad)
- ❌ `IniciarConversacionRequest.java`
- ❌ `ActualizarConversacionRequest.java`
- ❌ `ConversacionResponse.java`

### 2. Uso de Controladores Existentes

El microservicio ya proporciona los siguientes endpoints:

#### ✅ Crear Incidente
```http
POST /api/incidentes
Content-Type: application/json
X-API-Key: dev-key-12345

{
  "solicitante": {
    "nombreCompleto": "Juan Pérez",
    "telefono": "+59175123456",
    "canalOrigen": "telegram"
  },
  "ubicacion": {
    "descripcionTextual": "Av. Arce #1234, Zona Sur",
    "latitud": -16.5207,
    "longitud": -68.1193,
    "ciudad": "La Paz",
    "distrito": "Sur",
    "zona": "Obrajes"
  },
  "descripcionOriginal": "Bache grande en la calle que causa problemas al tráfico",
  "tipoIncidenteReportado": "Bache"
}
```

Respuesta:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "estado": "PENDIENTE_ANALISIS",
  "prioridad": null,
  "fechaReporte": "2025-11-10T10:30:00",
  "descripcionOriginal": "Bache grande en la calle...",
  "solicitante": {
    "id": "...",
    "nombreCompleto": "Juan Pérez",
    "telefono": "+59175123456"
  },
  "ubicacion": {
    "id": "...",
    "descripcionTextual": "Av. Arce #1234, Zona Sur",
    "latitud": -16.5207,
    "longitud": -68.1193
  }
}
```

#### ✅ Consultar Estado del Incidente
```http
GET /api/incidentes/{id}/detalle
X-API-Key: dev-key-12345
```

Respuesta:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "estado": "APROBADO",
  "prioridad": 8,
  "fechaReporte": "2025-11-10T10:30:00",
  "descripcionOriginal": "Bache grande en la calle...",
  "solicitante": { ... },
  "ubicacion": { ... },
  "multimedia": [
    {
      "id": "...",
      "url": "http://recepcion-app:8080/multimedia/archivos/...",
      "tipoArchivo": "image/jpeg"
    }
  ],
  "analisisTexto": {
    "tipoIncidente": "INFRAESTRUCTURA",
    "urgencia": 7,
    "sentimiento": "NEGATIVO"
  },
  "analisisImagen": {
    "severidad": 8,
    "confianza": 0.92
  },
  "historialEstados": [
    {
      "estado": "PENDIENTE_ANALISIS",
      "fechaCambio": "2025-11-10T10:30:00"
    },
    {
      "estado": "EN_ANALISIS",
      "fechaCambio": "2025-11-10T10:31:00"
    },
    {
      "estado": "APROBADO",
      "fechaCambio": "2025-11-10T10:35:00"
    }
  ]
}
```

#### ✅ Subir Multimedia
```http
POST /api/multimedia
Content-Type: multipart/form-data
X-API-Key: dev-key-12345

incidenteId: 123e4567-e89b-12d3-a456-426614174000
archivo: [binary data]
tipoArchivo: FOTO
descripcion: Foto del bache desde el frente
```

---

## Flujo de Trabajo Simplificado

### Arquitectura

```
┌─────────────────┐
│  Usuario Bot    │
│   Telegram      │
└────────┬────────┘
         │
         │ Mensajes
         ▼
┌─────────────────────────────────────────────┐
│              n8n Workflow                   │
│                                             │
│  ┌──────────────────────────────────────┐  │
│  │  Estado de Conversación (interno)    │  │
│  │  - Guardado en memoria de n8n        │  │
│  │  - Variables: nombre, teléfono, etc. │  │
│  └──────────────────────────────────────┘  │
│                                             │
│  1. Recolectar datos del usuario           │
│  2. Validar información                    │
│  3. Construir payload JSON                 │
│                                             │
└────────┬────────────────────────────────────┘
         │
         │ HTTP Requests
         ▼
┌──────────────────────────────────────────────┐
│       Microservicio Spring Boot              │
│                                              │
│  POST /api/incidentes  ──► IncidenteController│
│  POST /api/multimedia  ──► MultimediaController│
│  GET  /api/incidentes/{id}/detalle           │
│                                              │
│  ┌──────────────────────────────────────┐   │
│  │     Servicios Existentes             │   │
│  │  - IncidenteService                  │   │
│  │  - SolicitanteService                │   │
│  │  - UbicacionService                  │   │
│  │  - MultimediaService                 │   │
│  └──────────────────────────────────────┘   │
│                                              │
│  ┌──────────────────────────────────────┐   │
│  │     Base de Datos PostgreSQL         │   │
│  │  - Tabla: incidente                  │   │
│  │  - Tabla: solicitante                │   │
│  │  - Tabla: ubicacion                  │   │
│  │  - Tabla: multimedia                 │   │
│  │  - Tabla: historial_estados          │   │
│  └──────────────────────────────────────┘   │
│                                              │
└────────┬────────────────────────────┬────────┘
         │                            │
         │ HTTP                       │ HTTP
         ▼                            ▼
┌─────────────────┐         ┌─────────────────┐
│  Servicio ML    │         │  Servicio ML    │
│   Análisis      │         │   Análisis      │
│    Texto        │         │    Imagen       │
└─────────────────┘         └─────────────────┘
```

### Flujo Conversacional

```
Usuario: /start
  │
  ▼
n8n: "¡Hola! Ingresa tu nombre completo:"
  │
  ▼
Usuario: Juan Pérez
  │
  ▼
n8n: [Guarda nombre en memoria]
     "Ingresa tu número de teléfono:"
  │
  ▼
Usuario: +59175123456
  │
  ▼
n8n: [Guarda teléfono en memoria]
     "Describe el incidente:"
  │
  ▼
Usuario: Bache grande en la calle principal
  │
  ▼
n8n: [Guarda descripción en memoria]
     "Envía tu ubicación (GPS o texto):"
  │
  ▼
Usuario: [Envía GPS: -16.5207, -68.1193]
  │
  ▼
n8n: [Guarda ubicación en memoria]
     "¿Quieres agregar fotos? (Envía fotos o escribe NO)"
  │
  ▼
Usuario: [Envía foto]
  │
  ▼
n8n: [Guarda foto en array]
     "Foto recibida. Envía más fotos o escribe NO"
  │
  ▼
Usuario: NO
  │
  ▼
n8n: [Construye JSON y llama a POST /incidentes]
     ├─► POST /api/incidentes
     │   {
     │     "solicitante": { ... },
     │     "ubicacion": { ... },
     │     "descripcionOriginal": "..."
     │   }
     │
     ├─► [Recibe respuesta con ID del incidente]
     │
     ├─► [Si hay fotos] POST /api/multimedia (por cada foto)
     │
     └─► "✅ Incidente reportado!"
         "ID: 123e4567-..."
         "Estado: PENDIENTE_ANALISIS"
         "Te notificaremos cuando esté analizado."
```

---

## Gestión de Estado en n8n

n8n maneja el estado de la conversación **internamente** usando:

### Almacenamiento Global de Workflow

```javascript
// Obtener estado
const chatId = $input.item.json.message.chat.id;
const storageKey = `conversacion_${chatId}`;
const storage = $getWorkflowStaticData('global');
const estadoGuardado = storage[storageKey] || {};

// Guardar estado
storage[storageKey] = {
  estado: 'ESPERANDO_TELEFONO',
  nombre_completo: 'Juan Pérez',
  telefono: null,
  descripcion: null,
  ubicacion_texto: null,
  latitud: null,
  longitud: null,
  fotos: []
};

// Limpiar estado al finalizar
if (estado === 'FINALIZADO') {
  delete storage[storageKey];
}
```

### Estados de Conversación

| Estado | Descripción |
|--------|-------------|
| `NUEVO` | Usuario sin conversación activa |
| `ESPERANDO_NOMBRE` | Esperando nombre completo del usuario |
| `ESPERANDO_TELEFONO` | Esperando número de teléfono |
| `ESPERANDO_DESCRIPCION` | Esperando descripción del incidente |
| `ESPERANDO_UBICACION` | Esperando ubicación (GPS o texto) |
| `ESPERANDO_FOTOS` | Esperando fotos (opcional) |
| `FINALIZADO` | Incidente creado y conversación terminada |

---

## Ventajas de Esta Arquitectura

### ✅ Simplicidad
- No hay código duplicado
- No hay capa intermedia innecesaria
- Menos mantenimiento

### ✅ Separación de Responsabilidades
- **n8n**: Gestión de conversación y UX del bot
- **Microservicio**: Lógica de negocio, validaciones, persistencia
- **Servicios ML**: Análisis inteligente

### ✅ Escalabilidad
- n8n puede manejar múltiples canales (Telegram, WhatsApp, etc.)
- El microservicio no necesita saber de qué canal viene el incidente
- Los endpoints son reutilizables para cualquier cliente

### ✅ Mantenibilidad
- Cambios en el flujo conversacional solo afectan a n8n
- Cambios en la lógica de negocio solo afectan al microservicio
- Endpoints documentados y consistentes

---

## Configuración

### 1. Variables de Entorno (.env)

```env
# API Key para n8n
API_KEY_N8N=dev-key-12345

# URL del microservicio (desde n8n)
MICROSERVICIO_URL=http://recepcion-app:8080
```

### 2. Credenciales de n8n

#### Header Auth (API Key)
```
Name: X-API-Key
Value: dev-key-12345
```

#### Telegram Bot API
```
Access Token: [Token de BotFather]
```

### 3. Webhook de Telegram

Configurar en Telegram:
```
https://your-n8n-domain.com/webhook/telegram-webhook
```

---

## Endpoints del Microservicio

### Incidentes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/incidentes` | Crear nuevo incidente |
| GET | `/api/incidentes/{id}` | Obtener incidente (vista básica) |
| GET | `/api/incidentes/{id}/detalle` | Obtener incidente con detalles completos |
| PATCH | `/api/incidentes/{id}/estado` | Cambiar estado del incidente |
| GET | `/api/incidentes/estado/{estado}` | Listar incidentes por estado |
| GET | `/api/incidentes/pendientes-analisis` | Listar pendientes de análisis ML |

### Multimedia

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/multimedia` | Subir archivo multimedia |
| GET | `/api/multimedia/{id}` | Obtener metadatos de multimedia |
| GET | `/api/multimedia/archivos/{nombreArchivo}` | Descargar archivo |
| GET | `/api/multimedia/incidente/{incidenteId}` | Listar multimedia de un incidente |

### Solicitantes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/solicitantes/telefono/{telefono}` | Buscar solicitante por teléfono |
| GET | `/api/solicitantes/{id}` | Obtener solicitante por ID |

### Historial de Estados

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/historial-estados/incidente/{incidenteId}` | Ver historial de cambios de estado |

---

## Próximos Pasos

1. ✅ Eliminar código de `ConversacionBot` (COMPLETADO)
2. ✅ Crear workflow simplificado de n8n (COMPLETADO)
3. 🔄 Importar el workflow en n8n
4. 🔄 Configurar credenciales (API Key, Telegram Bot Token)
5. 🔄 Probar flujo completo:
   - Usuario reporta incidente
   - Se crea en el microservicio
   - Se envía a análisis ML
   - Se notifica resultado
6. 🔄 Agregar manejo de errores y validaciones
7. 🔄 Implementar notificaciones cuando el análisis ML esté completo

---

## Archivos Clave

- **Workflow de n8n**: `n8n/workflows/bot-telegram-SIMPLIFICADO.json`
- **Controller de Incidentes**: `recepcion/src/main/java/com/recepcion/recepcion/controller/IncidenteController.java`
- **DTO de Creación**: `recepcion/src/main/java/com/recepcion/recepcion/dto/request/CrearIncidenteRequest.java`
- **Configuración de Seguridad**: `recepcion/src/main/java/com/recepcion/recepcion/security/ApiKeyFilter.java`

---

## Conclusión

Esta arquitectura es **más simple, más mantenible y más escalable** que la anterior. Elimina la complejidad innecesaria y aprovecha correctamente los controladores existentes del microservicio.

**Principio clave**: Cada capa hace lo que mejor sabe hacer:
- n8n maneja la conversación
- El microservicio maneja la lógica de negocio
- No hay duplicación de responsabilidades
