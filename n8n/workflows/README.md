# 📋 Workflows de n8n

Esta carpeta contiene los workflows exportados para backup y versionamiento.

---

## 🔄 Workflows Disponibles

### 1. bot-whatsapp.json
**Estado:** Pendiente de creación
**Descripción:** Bot de WhatsApp para solicitud de ambulancias
**Trigger:** Webhook de WhatsApp Business API

**Flujo:**
```
WhatsApp Mensaje
    ↓
Webhook n8n (/webhook/whatsapp)
    ↓
Extraer datos (nombre, teléfono, descripción, ubicación, imagenes)
    ↓
Validar datos
    ↓
HTTP Request → POST /api/incidentes (Microservicio)
    ↓
Responder a usuario con código de incidente
```

### 2. bot-telegram.json
**Estado:** Pendiente de creación
**Descripción:** Bot de Telegram para solicitud de ambulancias
**Trigger:** Comando de Telegram

**Flujo:**
```
/start o /solicitar
    ↓
Bot solicita datos:
  - Nombre
  - Descripción del incidente
  - Ubicación (compartir ubicación)
    ↓
HTTP Request → POST /api/incidentes (Microservicio)
    ↓
Confirmación con botones inline
```

---

## 📥 Cómo Importar Workflows

Una vez que los workflows estén creados y exportados:

1. Abre n8n: http://localhost:5678
2. Clic en "+ New Workflow" o menú de workflows
3. Clic en el menú ⋮ → "Import from File"
4. Selecciona el archivo JSON correspondiente
5. Activa el workflow

---

## 📤 Cómo Exportar Workflows (Backup)

Para hacer backup de tus workflows:

1. Abre el workflow en n8n
2. Clic en el menú ⋮ (tres puntos)
3. Selecciona "Download"
4. Guarda el archivo en esta carpeta
5. Commit al repositorio Git

---

## 🛠️ Crear Workflows Desde Cero

### Template de Workflow: Bot de Solicitud de Ambulancia

#### Nodos necesarios:

1. **Webhook / Telegram Trigger**
   - Para recibir mensajes

2. **Function Node** (Extracción de datos)
   ```javascript
   // Extraer datos del mensaje
   const nombre = $input.item.json.message.from.first_name;
   const telefono = $input.item.json.message.from.username;
   const texto = $input.item.json.message.text;

   // Parsear descripción y ubicación del texto
   // O esperar que el usuario envíe ubicación

   return {
     json: {
       nombre,
       telefono,
       descripcion: texto,
       // ubicacion desde mensaje de ubicación
     }
   };
   ```

3. **HTTP Request Node** (Llamar al microservicio)
   ```
   Method: POST
   URL: {{ $env.MICROSERVICIO_BASE_URL }}/api/incidentes
   Headers:
     X-API-Key: {{ $env.MICROSERVICIO_API_KEY }}
     Content-Type: application/json
   Body:
   {
     "solicitante": {
       "nombre": "{{ $json.nombre }}",
       "telefono": "{{ $json.telefono }}"
     },
     "ubicacion": {
       "latitud": {{ $json.latitud }},
       "longitud": {{ $json.longitud }},
       "direccion": "{{ $json.direccion }}"
     },
     "descripcion": "{{ $json.descripcion }}",
     "tipo": "EMERGENCIA_MEDICA"
   }
   ```

4. **Telegram / WhatsApp Send Message Node**
   ```
   Mensaje de respuesta:
   ✅ Solicitud registrada exitosamente

   📋 Código: {{ $json.codigo }}
   🚑 Prioridad: {{ $json.prioridadFinal }}
   ⏰ Estado: {{ $json.estado }}

   La ambulancia está en camino.
   ```

---

## 🔗 Variables de Entorno en Workflows

Los workflows pueden usar variables de entorno definidas en `.env.n8n`:

```javascript
// En Function Nodes o HTTP Request Nodes:
{{ $env.MICROSERVICIO_BASE_URL }}     // http://microservicio:8080
{{ $env.MICROSERVICIO_API_KEY }}      // dev-key-12345
```

---

## 📚 Documentación de Referencia

- [n8n Workflow Templates](https://n8n.io/workflows)
- [n8n Function Node](https://docs.n8n.io/code-examples/expressions/functions/)
- [n8n HTTP Request Node](https://docs.n8n.io/integrations/builtin/core-nodes/n8n-nodes-base.httprequest/)

---

**Próximos pasos:**
1. Crear workflows en n8n UI
2. Probar con datos de ejemplo
3. Exportar y guardar aquí para versionamiento
4. Documentar cualquier personalización

---

**Última actualización:** 2025-01-07
