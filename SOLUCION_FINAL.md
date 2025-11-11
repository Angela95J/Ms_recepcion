# Solución Final: Bot Telegram con Análisis ML

## Resumen

Esta es la solución correcta que integra el bot de Telegram (n8n) con el microservicio de recepción de incidentes, incluyendo análisis ML y notificación de resultados al usuario.

---

## Cambios Realizados

### 1. Eliminación de Código Innecesario

Se eliminaron todos los archivos relacionados con `ConversacionBot` que duplicaban funcionalidad:

- ❌ `ConversacionBotController.java`
- ❌ `ConversacionBotService.java`
- ❌ `ConversacionBotRepository.java`
- ❌ `ConversacionBot.java` (entidad)
- ❌ `IniciarConversacionRequest.java`
- ❌ `ActualizarConversacionRequest.java`
- ❌ `ConversacionResponse.java`

### 2. Workflow Correcto de n8n

**Archivo**: `n8n/workflows/bot-telegram-CORREGIDO.json`

Este workflow implementa:
- ✅ Gestión de estado **interna en n8n** (usando `$getWorkflowStaticData`)
- ✅ Recolección paso a paso de datos
- ✅ Creación de incidente usando controladores existentes
- ✅ **Espera y notificación de resultados ML**
- ✅ **Mensaje personalizado según prioridad**

---

## Flujo Completo del Bot

### Diagrama de Flujo

```
┌──────────────────────────────────────────────────────────────┐
│                      USUARIO (TELEGRAM)                      │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            │ /reportar
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                    n8n WORKFLOW                              │
│                                                              │
│  [Paso 1] Recolectar Nombre                                 │
│     ↓                                                        │
│  [Paso 2] Recolectar Teléfono                               │
│     ↓                                                        │
│  [Paso 3] Recolectar Descripción                            │
│     ↓                                                        │
│  [Paso 4] Recolectar Ubicación GPS                          │
│     ↓                                                        │
│  [Paso 5] Recolectar Fotos (opcional)                       │
│     ↓                                                        │
│  [Estado guardado internamente en n8n]                      │
│                                                              │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            │ Usuario escribe "LISTO"
                            ▼
┌──────────────────────────────────────────────────────────────┐
│            POST /incidentes (Microservicio)                  │
│                                                              │
│  Payload:                                                    │
│  {                                                           │
│    "solicitante": {                                          │
│      "nombreCompleto": "...",                                │
│      "telefono": "...",                                      │
│      "canalOrigen": "TELEGRAM"                               │
│    },                                                        │
│    "ubicacion": {                                            │
│      "descripcionTextual": "GPS: lat, lon",                  │
│      "latitud": -16.5207,                                    │
│      "longitud": -68.1193                                    │
│    },                                                        │
│    "descripcionOriginal": "...",                             │
│    "tipoIncidenteReportado": "INCIDENTE_GENERAL"            │
│  }                                                           │
│                                                              │
│  Respuesta:                                                  │
│  {                                                           │
│    "id": "123e4567-...",                                     │
│    "estado": "PENDIENTE_ANALISIS",                           │
│    "prioridad": null  ← Aún sin analizar                     │
│  }                                                           │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│         NOTIFICAR AL USUARIO: "Analizando con IA..."         │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                  ESPERAR 10 SEGUNDOS                         │
│             (Mientras ML analiza el incidente)               │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│      GET /incidentes/{id}/detalle (Microservicio)           │
│                                                              │
│  Verificar si análisis ML está completo:                    │
│  - Si prioridad !== null → ML terminó ✅                     │
│  - Si prioridad === null → ML en proceso, esperar 5seg más  │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            │ ML Completado
                            ▼
┌──────────────────────────────────────────────────────────────┐
│              EVALUAR PRIORIDAD Y CREAR MENSAJE               │
│                                                              │
│  Prioridad >= 8 → 🚨 CRÍTICA                                 │
│  Prioridad >= 6 → 🔴 ALTA                                    │
│  Prioridad >= 4 → 🟡 MEDIA                                   │
│  Prioridad < 4  → 🔵 BAJA                                    │
│                                                              │
│  Incluir en mensaje:                                         │
│  - ID del incidente                                          │
│  - Prioridad calculada                                       │
│  - Análisis de texto (tipo, urgencia, sentimiento)          │
│  - Análisis de imagen (severidad, confianza)                │
│  - Mensaje personalizado según urgencia                     │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│         NOTIFICAR RESULTADO AL USUARIO (Telegram)            │
│                                                              │
│  Ejemplo de mensaje:                                         │
│  🚨 *PRIORIDAD CRÍTICA* 🚨                                   │
│                                                              │
│  🆔 ID: `123e4567-...`                                       │
│  ⚡ Prioridad: *8/10*                                        │
│  📊 Urgencia: *CRÍTICA*                                      │
│  📅 Estado: APROBADO                                         │
│                                                              │
│  📝 *Análisis de Texto:*                                     │
│  • Tipo: INFRAESTRUCTURA                                     │
│  • Urgencia detectada: 7/10                                  │
│  • Sentimiento: NEGATIVO                                     │
│                                                              │
│  📸 *Análisis de Imagen:*                                    │
│  • Severidad: 8/10                                           │
│  • Confianza: 92%                                            │
│                                                              │
│  ⚠️ *ATENCIÓN INMEDIATA REQUERIDA*                           │
│  🚑 El incidente requiere atención urgente                   │
│  ⏱️ Será procesado con máxima prioridad                      │
│  📞 Nos contactaremos contigo pronto                         │
│                                                              │
│  _Gracias por usar nuestro servicio_                         │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│            LIMPIAR ESTADO DE CONVERSACIÓN                    │
│         (Eliminar datos temporales de n8n)                   │
└──────────────────────────────────────────────────────────────┘
```

---

## Conversación de Ejemplo

```
👤 Usuario: /reportar

🤖 Bot: 🚨 *REPORTE DE INCIDENTE* 🚨

       1️⃣ Envía tu *nombre completo*:

👤 Usuario: Juan Pérez

🤖 Bot: ✅ Nombre: *Juan Pérez*

       2️⃣ Envía tu *número de teléfono* (ej: +59175123456):

👤 Usuario: +59175123456

🤖 Bot: ✅ Teléfono registrado

       3️⃣ *Describe el incidente* con detalle:

👤 Usuario: Bache grande en la calle principal que causa problemas al tráfico

🤖 Bot: ✅ Descripción registrada

       4️⃣ *Comparte ubicación GPS*

       📎 → Ubicación

👤 Usuario: [Envía ubicación GPS]

🤖 Bot: ✅ Ubicación recibida

       5️⃣ *Fotos* (opcional)

       Envía fotos o escribe *LISTO*

👤 Usuario: [Envía foto del bache]

🤖 Bot: ✅ Foto 1 recibida

       ¿Más fotos? Envía o escribe *LISTO*

👤 Usuario: LISTO

🤖 Bot: ⏳ *Incidente registrado*

       🆔 ID: `123e4567-e89b-12d3-a456-426614174000`
       📊 Estado: PENDIENTE_ANALISIS

       🤖 *Analizando con IA...*
       📝 Análisis de texto
       📸 Análisis de imágenes
       ⚡ Calculando prioridad

       _Espera un momento por favor_

[n8n espera 10 segundos mientras ML analiza]

[n8n consulta GET /incidentes/{id}/detalle cada 5 segundos hasta que prioridad !== null]

🤖 Bot: 🚨 *PRIORIDAD ALTA* 🚨

       🆔 ID: `123e4567-e89b-12d3-a456-426614174000`
       ⚡ Prioridad: *7/10*
       📊 Urgencia: *ALTA*
       📅 Estado: APROBADO

       📝 *Análisis de Texto:*
       • Tipo: INFRAESTRUCTURA
       • Urgencia detectada: 6/10
       • Sentimiento: NEGATIVO

       📸 *Análisis de Imagen:*
       • Severidad: 8/10
       • Confianza: 89%

       ⚡ *ATENCIÓN PRIORITARIA*

       📋 El incidente será atendido con alta prioridad
       ⏱️ Procesamiento acelerado

       📞 Recibirás una respuesta pronto

       _Gracias por usar nuestro servicio_
```

---

## Arquitectura Técnica

### Componentes

```
┌─────────────────────────────────────────────────────────────┐
│                         n8n                                 │
│                                                             │
│  - Telegram Trigger (recibe mensajes)                      │
│  - Lógica de conversación (Function nodes)                 │
│  - Almacenamiento de estado ($getWorkflowStaticData)       │
│  - HTTP Request nodes (llamadas al microservicio)          │
│  - Wait nodes (polling de resultados ML)                   │
│  - Telegram nodes (envío de mensajes)                      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ HTTP Requests
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              Microservicio Spring Boot                      │
│                                                             │
│  Controllers:                                               │
│  ├─ POST   /incidentes          ← Crear incidente          │
│  ├─ GET    /incidentes/{id}     ← Consultar incidente      │
│  └─ GET    /incidentes/{id}/detalle ← Detalle completo     │
│                                                             │
│  Services:                                                  │
│  ├─ IncidenteService  (lógica de negocio)                  │
│  ├─ SolicitanteService                                     │
│  ├─ UbicacionService                                       │
│  └─ MultimediaService                                      │
│                                                             │
│  Base de Datos (PostgreSQL):                               │
│  ├─ incidente                                              │
│  ├─ solicitante                                            │
│  ├─ ubicacion                                              │
│  ├─ multimedia                                             │
│  ├─ analisis_texto                                         │
│  ├─ analisis_imagen                                        │
│  └─ historial_estados                                      │
└────────────┬──────────────────────┬─────────────────────────┘
             │                      │
             │ HTTP                 │ HTTP
             ▼                      ▼
┌────────────────────┐   ┌────────────────────┐
│  Servicio ML       │   │  Servicio ML       │
│  Análisis Texto    │   │  Análisis Imagen   │
│  (Puerto 8001)     │   │  (Puerto 8002)     │
└────────────────────┘   └────────────────────┘
```

### Gestión de Estado

n8n maneja el estado de la conversación **internamente**:

```javascript
// Obtener estado
const userId = $input.item.json.userId;
const storageKey = `conversacion_${userId}`;
const storage = $getWorkflowStaticData('global');
const estadoGuardado = storage[storageKey] || {
  pasoActual: 'inicio',
  datosRecolectados: {}
};

// Guardar estado
storage[storageKey] = {
  pasoActual: 'esperando_telefono',
  datosRecolectados: {
    nombre: 'Juan Pérez',
    username: '@juanperez',
    chatId: 123456789
  }
};

// Limpiar estado al finalizar
delete storage[storageKey];
```

### Polling de Resultados ML

El workflow implementa un mecanismo de polling para esperar los resultados del análisis ML:

```
1. POST /incidentes → Crea incidente (prioridad = null)
2. Notificar usuario: "Analizando con IA..."
3. Esperar 10 segundos
4. GET /incidentes/{id}/detalle
5. ¿prioridad !== null?
   - SÍ → Análisis completo, notificar resultado ✅
   - NO → Esperar 5 segundos más, volver al paso 4 🔄
```

---

## Endpoints del Microservicio Utilizados

### 1. Crear Incidente

**Desde n8n (dentro de Docker)**:
```http
POST http://microservicio:8080/api/incidentes
X-API-Key: dev-key-12345
Content-Type: application/json

{
  "solicitante": {
    "nombreCompleto": "Juan Pérez",
    "telefono": "+59175123456",
    "canalOrigen": "TELEGRAM"
  },
  "ubicacion": {
    "descripcionTextual": "GPS: -16.520700, -68.119300",
    "latitud": -16.5207,
    "longitud": -68.1193
  },
  "descripcionOriginal": "Bache grande en la calle principal",
  "tipoIncidenteReportado": "INCIDENTE_GENERAL"
}
```

**Respuesta:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "estado": "PENDIENTE_ANALISIS",
  "prioridad": null,
  "fechaReporte": "2025-11-10T10:30:00"
}
```

### 2. Consultar Detalle del Incidente (con resultados ML)

**Desde n8n (dentro de Docker)**:
```http
GET http://microservicio:8080/api/incidentes/123e4567-e89b-12d3-a456-426614174000/detalle
X-API-Key: dev-key-12345
```

**Respuesta (después del análisis ML):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "estado": "APROBADO",
  "prioridad": 7,
  "fechaReporte": "2025-11-10T10:30:00",
  "descripcionOriginal": "Bache grande en la calle principal",
  "solicitante": {
    "id": "...",
    "nombreCompleto": "Juan Pérez",
    "telefono": "+59175123456",
    "canalOrigen": "TELEGRAM"
  },
  "ubicacion": {
    "id": "...",
    "descripcionTextual": "GPS: -16.520700, -68.119300",
    "latitud": -16.5207,
    "longitud": -68.1193
  },
  "analisisTexto": {
    "id": "...",
    "tipoIncidente": "INFRAESTRUCTURA",
    "urgencia": 6,
    "sentimiento": "NEGATIVO",
    "palabrasClave": ["bache", "calle", "tráfico"]
  },
  "analisisImagen": {
    "id": "...",
    "severidad": 8,
    "confianza": 0.89,
    "descripcion": "Daño severo en pavimento"
  },
  "multimedia": [
    {
      "id": "...",
      "url": "http://recepcion-app:8080/multimedia/archivos/...",
      "tipoArchivo": "image/jpeg"
    }
  ],
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

---

## URLs Correctas para la Conexión

### Desde n8n (dentro de Docker)

n8n se comunica con el microservicio usando el **nombre del servicio** en Docker:

```
✅ POST http://microservicio:8080/api/incidentes
✅ GET  http://microservicio:8080/api/incidentes/{id}/detalle
```

**Explicación**:
- `microservicio` → Nombre del servicio en `docker-compose.app.yml`
- `8080` → Puerto interno del contenedor
- `/api` → Context path configurado en `application.yml`
- `/incidentes` → Ruta del `IncidenteController`

### Desde fuera de Docker (Postman, navegador)

```
✅ POST http://localhost:8080/api/incidentes
✅ GET  http://localhost:8080/api/incidentes/{id}/detalle
```

**Explicación**:
- `localhost` → Acceso desde tu máquina
- `8080` → Puerto mapeado en `docker-compose.app.yml`

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

#### a) Header Auth (API Key)
```
Name: X-API-Key
Value: dev-key-12345
```

#### b) Telegram Bot API
```
Access Token: [Token obtenido de @BotFather]
```

### 3. Configurar Webhook de Telegram

1. Obtener token de @BotFather
2. Activar el workflow en n8n
3. Configurar el webhook en Telegram (n8n lo hace automáticamente con telegramTrigger)

---

## Ventajas de Esta Solución

### ✅ Arquitectura Limpia
- Sin código duplicado
- Separación clara de responsabilidades
- Cada componente hace lo que mejor sabe hacer

### ✅ Experiencia de Usuario Completa
- Recolección paso a paso guiada
- Feedback inmediato en cada paso
- Notificación de análisis en progreso
- **Resultado detallado del análisis ML**
- Mensajes personalizados según prioridad

### ✅ Escalabilidad
- n8n puede manejar múltiples canales (Telegram, WhatsApp, etc.)
- El microservicio es independiente del canal
- Fácil agregar nuevos tipos de análisis

### ✅ Mantenibilidad
- Cambios en el flujo conversacional solo afectan a n8n
- Cambios en la lógica de negocio solo afectan al microservicio
- Endpoints bien definidos y documentados

---

## Próximos Pasos

1. ✅ Código innecesario eliminado
2. ✅ Workflow completo creado
3. 🔄 **Importar workflow en n8n**
   - Ir a n8n
   - Workflows → Import from File
   - Seleccionar `n8n/workflows/bot-telegram-CORREGIDO.json`
4. 🔄 **Configurar credenciales**
   - Crear credencial "Telegram Bot" con token de @BotFather
   - Crear credencial "Header Auth" con X-API-Key: dev-key-12345
5. 🔄 **Activar workflow**
   - Clic en "Active" en la esquina superior derecha
6. 🔄 **Probar flujo completo**
   - Enviar `/reportar` al bot de Telegram
   - Completar todos los pasos
   - Verificar que se recibe la notificación con resultados ML
7. 🔄 **Verificar análisis ML**
   - Comprobar que los servicios ML están corriendo (puertos 8001 y 8002)
   - Revisar logs del microservicio para ver las llamadas a ML
   - Confirmar que la prioridad se calcula correctamente

---

## Archivos Clave

- **Workflow n8n**: `n8n/workflows/bot-telegram-CORREGIDO.json`
- **Documentación**: `ARQUITECTURA_CORRECTA.md`
- **Este archivo**: `SOLUCION_FINAL.md`
- **Controller de Incidentes**: `recepcion/src/main/java/com/recepcion/recepcion/controller/IncidenteController.java`
- **Configuración**: `.env`

---

## Conclusión

Esta solución implementa correctamente la integración entre el bot de Telegram y el microservicio de recepción, siguiendo las mejores prácticas:

1. **Sin duplicación de código** - Eliminamos ConversacionBot innecesario
2. **Estado manejado en n8n** - No sobrecargamos el microservicio
3. **Uso de controladores existentes** - Aprovechamos lo que ya estaba implementado
4. **Experiencia de usuario completa** - Desde el reporte hasta la notificación de resultados ML
5. **Arquitectura escalable** - Fácil agregar nuevos canales o tipos de análisis

**La solución está lista para ser probada e implementada.**
