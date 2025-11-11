# 🚑 Flujo Completo del Bot con Notificación ML

## 📊 Flujo Visual

```
Usuario: /solicitar
    ↓
Bot: "Envía tu nombre"
    ↓
Usuario: "María García"
    ↓
Bot: "Describe la emergencia"
    ↓
Usuario: "Dolor en el pecho, dificultad respirar"
    ↓
Bot: "Comparte tu ubicación GPS"
    ↓
Usuario: [Comparte GPS]
    ↓
Bot: "Fotos? (opcional)"
    ↓
Usuario: [Foto] o "LISTO"
    ↓
┌──────────────────────────────────┐
│  Crear Incidente en BD           │
└──────────┬───────────────────────┘
           │
           ▼
┌──────────────────────────────────┐
│  Bot: "⏳ Solicitud registrada   │
│        Código: AMB-2024-001      │
│        Analizando con IA..."     │
└──────────┬───────────────────────┘
           │
           ▼
┌──────────────────────────────────┐
│  Esperar 10 segundos             │ ◄── Dar tiempo al ML
└──────────┬───────────────────────┘
           │
           ▼
┌──────────────────────────────────┐
│  Consultar Incidente             │
│  GET /api/incidentes/{id}        │
└──────────┬───────────────────────┘
           │
           ▼
      ¿prioridadFinal existe?
           │
    ┌──────┴──────┐
    │NO           │SÍ
    ▼             ▼
Esperar 5seg   Evaluar Prioridad
    │             │
    └─────►───────┘
                  │
                  ▼
         ┌────────┴────────┐
         │  CRÍTICA/ALTA?  │
         └────────┬────────┘
                  │
    ┌─────────────┼─────────────┐
    │             │             │
    ▼             ▼             ▼
CRÍTICA        ALTA         MEDIA/BAJA
    │             │             │
    ▼             ▼             ▼
"🚨 EMERGENCIA   "🚨 EMERGENCIA  "🟡/🔵
CRÍTICA          ALTA           ...
Ambulancia      Ambulancia      Evaluando
INMEDIATAMENTE  DE INMEDIATO    disponibilidad
5-10 min"       10-20 min"      20-60 min"
    │             │             │
    └─────────────┼─────────────┘
                  │
                  ▼
        ┌─────────────────┐
        │ Finalizar Conv. │
        └─────────────────┘
```

## 🎯 Mensajes Según Prioridad

### 🚨 CRÍTICA
```
🚨 EMERGENCIA CRÍTICA 🚨

📋 Código: `AMB-2024-001`
⚡ Urgencia: MÁXIMA

Ambulancia despachada INMEDIATAMENTE

⏱️ Tiempo estimado: 5-10 minutos
🚑 Unidad de emergencia en camino

⚠️ MANTENTE EN LA UBICACIÓN
📞 Te llamaremos si necesitamos más información

Gracias por usar nuestro servicio
```

### 🚨 ALTA
```
🚨 EMERGENCIA ALTA 🚨

📋 Código: `AMB-2024-002`
⚡ Urgencia: ALTA

Ambulancia despachada DE INMEDIATO

⏱️ Tiempo estimado: 10-20 minutos
🚑 Prioridad alta confirmada

✅ Mantente en la ubicación
📞 Mantén tu teléfono disponible

Gracias por usar nuestro servicio
```

### 🟡 MEDIA
```
🟡 EMERGENCIA MODERADA 🟡

📋 Código: `AMB-2024-003`
⚡ Urgencia: MEDIA

Ambulancia en camino

⏱️ Tiempo estimado: 20-40 minutos
🚑 Unidad asignada

✅ Mantente tranquilo
📞 Te contactaremos pronto

Gracias por usar nuestro servicio
```

### 🔵 BAJA
```
🔵 SOLICITUD REGISTRADA 🔵

📋 Código: `AMB-2024-004`
⚡ Urgencia: BAJA

Evaluando disponibilidad de unidades

⏱️ Tiempo estimado: 40-60 minutos
📋 Solicitud en cola

💡 Si la situación empeora, llama al 911
📞 Te contactaremos para confirmar

Gracias por usar nuestro servicio
```

## 🔄 Loop de Espera ML

El workflow implementa un **polling inteligente**:

1. **Primera espera:** 10 segundos (para que ML procese)
2. **Consulta incidente:** `GET /api/incidentes/{id}`
3. **Verifica:** ¿Tiene `prioridadFinal`?
   - **SÍ** → Notifica al usuario
   - **NO** → Espera 5 segundos más y vuelve al paso 2

**Ventajas:**
- ✅ No sobrecarga el servidor
- ✅ Notifica apenas está listo
- ✅ Timeout implícito (n8n tiene límite de ejecución)

## 🧪 Ejemplo de Prueba Completa

```
Usuario: /solicitar
Bot: 🚨 SOLICITUD DE AMBULANCIA
     1️⃣ Envía tu nombre completo:

Usuario: Carlos Mendoza
Bot: ✅ Nombre: Carlos Mendoza
     2️⃣ Describe la emergencia:

Usuario: Accidente de tránsito, sangrado en la pierna
Bot: ✅ Descripción registrada
     3️⃣ Comparte ubicación GPS
     📎 → Ubicación

Usuario: [Ubicación GPS: -17.3945, -66.1570]
Bot: ✅ Ubicación recibida
     4️⃣ Fotos (opcional)
     Envía fotos o escribe LISTO

Usuario: [Envía 2 fotos]
Bot: ✅ Foto 1 recibida
     ¿Más fotos?

Bot: ✅ Foto 2 recibida
     ¿Más fotos?

Usuario: LISTO
Bot: ⏳ Solicitud registrada
     📋 Código: AMB-2024-125
     🆔 ID: 456
     
     🤖 Analizando con IA...
     ⚕️ Evaluando gravedad
     📊 Procesando datos
     
     Espera un momento por favor

[Espera 10-15 segundos]

Bot: 🚨 EMERGENCIA ALTA 🚨
     
     📋 Código: AMB-2024-125
     ⚡ Urgencia: ALTA
     
     Ambulancia despachada DE INMEDIATO
     
     ⏱️ Tiempo estimado: 10-20 minutos
     🚑 Prioridad alta confirmada
     
     ✅ Mantente en la ubicación
     📞 Mantén tu teléfono disponible
     
     Gracias por usar nuestro servicio
```

## 📁 Archivos Relacionados

- **bot-telegram-final.json** - Workflow completo con notificación ML
- **bot-telegram-v3.json** - Versión sin notificación ML (backup)
- **bot-telegram-v2.json** - Versión anterior
- **bot-telegram.json** - Versión original

## ⚙️ Configuración

### Variables de entorno necesarias:
```env
API_KEY_N8N=dev-key-12345
MICROSERVICIO_PORT=8080
```

### Credenciales n8n:
1. Token de @BotFather
2. Vincular a todos los nodos Telegram

### Endpoints requeridos:
```
✅ POST   /api/conversaciones/iniciar
✅ PATCH  /api/conversaciones/{userId}?canal=TELEGRAM
✅ GET    /api/conversaciones/{userId}?canal=TELEGRAM
✅ POST   /api/conversaciones/{userId}/finalizar
✅ DELETE /api/conversaciones/{userId}?canal=TELEGRAM
✅ POST   /api/incidentes
✅ GET    /api/incidentes/{id}
```

## 🎬 Cómo Importar

1. Abrir n8n: http://localhost:5678
2. Workflows → New Workflow
3. Import from File → Seleccionar `bot-telegram-final.json`
4. Configurar credenciales Telegram
5. Activar workflow (toggle verde)
6. ¡Probar con tu bot!

## 🐛 Troubleshooting

### Bot no notifica después de crear incidente
- Verificar que ML services estén corriendo
- Revisar logs: `docker logs recepcion-ml-texto`
- Verificar que endpoint GET /api/incidentes/{id} funcione

### Loop infinito en espera ML
- n8n tiene timeout automático (~5 min)
- Si ML tarda mucho, ajustar tiempos de espera
- Considerar agregar contador de reintentos

### Error en finalizar conversación
- Verificar que incidenteId se pase correctamente
- Revisar logs del microservicio
- Confirmar que conversación existe

## 🌟 Características Clave

✅ **Comandos de ayuda** (/start, /ayuda, /cancelar)
✅ **Validaciones** en cada paso
✅ **Flujo secuencial** guiado
✅ **Notificación ML** con prioridades
✅ **Mensajes personalizados** según urgencia
✅ **Gestión de estado** persistente
✅ **Manejo de fotos** múltiples
✅ **Timeout automático** de conversaciones

