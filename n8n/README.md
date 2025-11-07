# 🤖 n8n - Bot de Solicitud de Ambulancias

Esta carpeta contiene los workflows y configuraciones de n8n para el bot de WhatsApp/Telegram que permite a los usuarios solicitar ambulancias.

---

## 📁 Estructura

```
n8n/
├── workflows/               # Workflows exportados (backup y versionamiento)
│   ├── bot-whatsapp.json   # Workflow del bot de WhatsApp
│   ├── bot-telegram.json   # Workflow del bot de Telegram
│   └── README.md           # Documentación de workflows
│
├── credentials/            # Documentación de credenciales
│   └── README.md          # Cómo configurar credenciales
│
└── README.md              # Este archivo
```

---

## 🚀 Inicio Rápido

### 1. Levantar n8n
```bash
# Windows
start-n8n.bat

# Linux/Mac
./start-n8n.sh

# Manual
docker-compose -f docker-compose.n8n.yml up -d
```

### 2. Acceder a n8n
Abre tu navegador en: **http://localhost:5678**

**Credenciales por defecto:**
- Usuario: `admin`
- Contraseña: `admin123`

⚠️ **IMPORTANTE:** Cambia las credenciales en producción editando `.env.n8n`

---

## 🔧 Configuración Inicial

### Paso 1: Configurar Credenciales

En n8n, ve a: **Settings → Credentials**

#### Para WhatsApp Business API:
1. Crear credential tipo "WhatsApp Business Cloud API"
2. Ingresar:
   - Phone Number ID
   - Access Token
   - Webhook Verify Token

#### Para Telegram:
1. Crear credential tipo "Telegram"
2. Ingresar:
   - Bot Token (obtener de @BotFather en Telegram)

Ver: [credentials/README.md](credentials/README.md) para instrucciones detalladas.

### Paso 2: Configurar Webhooks

Para que los bots funcionen, necesitas configurar webhooks:

#### WhatsApp:
```
Webhook URL: http://TU-DOMINIO:5678/webhook/whatsapp
Verify Token: (el que definiste en credentials)
```

#### Telegram:
```
Webhook URL: http://TU-DOMINIO:5678/webhook/telegram
```

⚠️ **Nota:** En desarrollo local, usa **ngrok** o **localtunnel** para exponer tu puerto 5678 a internet.

---

## 📋 Workflows Disponibles

### 1. Bot de WhatsApp (bot-whatsapp.json)

**Funcionalidad:**
- Recibe mensajes de usuarios vía WhatsApp
- Extrae información del incidente (descripción, ubicación)
- Llama al microservicio para crear el incidente
- Responde al usuario con el código del incidente

**Endpoints:**
- Webhook: `/webhook/whatsapp`
- Trigger: Mensaje entrante de WhatsApp

### 2. Bot de Telegram (bot-telegram.json)

**Funcionalidad:**
- Similar al bot de WhatsApp
- Soporta comandos: `/start`, `/ayuda`, `/solicitar`
- Maneja ubicación compartida
- Confirmación con botones inline

**Endpoints:**
- Webhook: `/webhook/telegram`
- Trigger: Mensaje/comando de Telegram

---

## 🔗 Integración con Microservicio

Los workflows se comunican con el microservicio usando:

```javascript
// Configuración en n8n HTTP Request Node
URL: {{ $env.MICROSERVICIO_BASE_URL }}/api/incidentes
Method: POST
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

---

## 📤 Exportar/Importar Workflows

### Exportar (Backup)
1. En n8n, abre el workflow
2. Clic en el menú ⋮ → Download
3. Guarda el archivo JSON en `n8n/workflows/`
4. Commit al repositorio Git

### Importar
1. En n8n, clic en "+ New Workflow"
2. Clic en el menú ⋮ → Import from File
3. Selecciona el archivo JSON de `n8n/workflows/`

---

## 🐛 Troubleshooting

### Problema: n8n no inicia
```bash
# Ver logs
docker-compose -f docker-compose.n8n.yml logs -f

# Verificar que la red existe
docker network ls | grep recepcion-network

# Si no existe, crear primero el stack principal
docker-compose -f docker-compose.app.yml up -d
```

### Problema: No puede conectarse al microservicio
```bash
# Verificar que ambos stacks están en la misma red
docker inspect recepcion-n8n | grep Networks
docker inspect recepcion-microservicio | grep Networks

# Deben estar ambos en "recepcion-network"
```

### Problema: Webhooks no funcionan
1. Verificar que el puerto 5678 está expuesto
2. En producción, usar dominio público (no localhost)
3. Usar ngrok en desarrollo:
   ```bash
   ngrok http 5678
   # Usar la URL de ngrok como WEBHOOK_URL
   ```

---

## 🔐 Seguridad

### Variables de entorno sensibles

Las siguientes variables están en `.env.n8n`:
```env
N8N_BASIC_AUTH_USER=admin
N8N_BASIC_AUTH_PASSWORD=admin123
MICROSERVICIO_API_KEY=dev-key-12345
```

⚠️ **En producción:**
1. Cambia las contraseñas
2. Usa HTTPS (N8N_PROTOCOL=https)
3. Configura SSL/TLS
4. Restringe acceso por IP si es posible

---

## 📚 Recursos

- [Documentación oficial de n8n](https://docs.n8n.io/)
- [n8n WhatsApp Node](https://docs.n8n.io/integrations/builtin/app-nodes/n8n-nodes-base.whatsapp/)
- [n8n Telegram Node](https://docs.n8n.io/integrations/builtin/app-nodes/n8n-nodes-base.telegram/)
- [Crear bot de Telegram con BotFather](https://core.telegram.org/bots/tutorial)
- [WhatsApp Business API](https://developers.facebook.com/docs/whatsapp/cloud-api/)

---

## 🤝 Soporte

Para problemas con n8n:
1. Revisa los logs: `docker-compose -f docker-compose.n8n.yml logs -f`
2. Consulta la documentación: [N8N_README.md](../N8N_README.md)
3. Verifica la integración con el microservicio

---

**Última actualización:** 2025-01-07
