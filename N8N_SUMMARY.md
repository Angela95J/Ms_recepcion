# 📦 Resumen de Dockerización de n8n

## ✅ Archivos Creados

### 🐳 **Docker Compose**
```
✓ docker-compose.n8n.yml           # Orquestador de n8n
```

**Configuración:**
- Imagen: `n8nio/n8n:latest`
- Puerto: 5678
- Base de datos: SQLite (interno)
- Red: `recepcion-network` (compartida con stack principal)
- Volumen: `n8n_data` (persistente)

---

### ⚙️ **Configuración**
```
✓ .env.n8n                         # Variables de entorno (activo)
✓ .env.n8n.example                 # Template de configuración
✓ .gitignore                       # Actualizado con reglas de n8n
```

**Variables principales:**
- `N8N_PORT=5678`
- `N8N_BASIC_AUTH_USER=admin`
- `N8N_BASIC_AUTH_PASSWORD=admin123`
- `WEBHOOK_URL=http://localhost:5678`
- `MICROSERVICIO_BASE_URL=http://microservicio:8080`
- `MICROSERVICIO_API_KEY=dev-key-12345`

---

### 🚀 **Scripts de Gestión (10 archivos)**

**Scripts de n8n (Windows):**
```
✓ start-n8n.bat                    # Iniciar n8n
✓ stop-n8n.bat                     # Detener n8n
✓ logs-n8n.bat                     # Ver logs
```

**Scripts de n8n (Linux/Mac):**
```
✓ start-n8n.sh                     # Iniciar n8n
✓ stop-n8n.sh                      # Detener n8n
✓ logs-n8n.sh                      # Ver logs
```

**Scripts para ambos stacks (Windows):**
```
✓ start-all.bat                    # Iniciar App + n8n
✓ stop-all.bat                     # Detener todo
```

**Scripts para ambos stacks (Linux/Mac):**
```
✓ start-all.sh                     # Iniciar App + n8n
✓ stop-all.sh                      # Detener todo
```

---

### 📁 **Estructura de n8n**
```
✓ n8n/                             # Carpeta principal
✓ n8n/README.md                    # Documentación principal
✓ n8n/workflows/                   # Workflows exportados (backup)
✓ n8n/workflows/README.md          # Guía de workflows
✓ n8n/credentials/                 # Documentación de credenciales
✓ n8n/credentials/README.md        # Cómo configurar WhatsApp/Telegram
```

---

### 📚 **Documentación (3 archivos)**
```
✓ N8N_README.md                    # Inicio rápido de n8n
✓ N8N_SUMMARY.md                   # Este archivo (resumen)
✓ n8n/README.md                    # Guía completa de n8n
```

---

## 🏗️ **Arquitectura Implementada**

### **2 Stacks Docker Independientes**

```
┌─────────────────────────────────────────────────────────┐
│                    DOCKER HOST                          │
│                                                         │
│  ┌────────────────────────────────────────────────┐   │
│  │  Stack 1: docker-compose.app.yml               │   │
│  │  ┌──────────────────────────────────────────┐  │   │
│  │  │  • postgres (5432)                       │  │   │
│  │  │  • microservicio (8080)                  │  │   │
│  │  │  • ml-texto (8001)                       │  │   │
│  │  │  • ml-imagen (8002)                      │  │   │
│  │  └──────────────────────────────────────────┘  │   │
│  └────────────────────────────────────────────────┘   │
│                         │                              │
│              Red: recepcion-network (compartida)       │
│                         │                              │
│  ┌────────────────────────────────────────────────┐   │
│  │  Stack 2: docker-compose.n8n.yml               │   │
│  │  ┌──────────────────────────────────────────┐  │   │
│  │  │  • n8n (5678)                            │  │   │
│  │  │    - Bot WhatsApp                        │  │   │
│  │  │    - Bot Telegram                        │  │   │
│  │  │    - Workflows                           │  │   │
│  │  └──────────────────────────────────────────┘  │   │
│  └────────────────────────────────────────────────┘   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 🔗 **Comunicación Entre Stacks**

### **Red Compartida:**
- Nombre: `recepcion-network`
- Tipo: bridge
- Creada por: `docker-compose.app.yml`
- Usada por: `docker-compose.n8n.yml` (external: true)

### **DNS Interno:**
```
n8n → microservicio:8080          ✅ Funciona
n8n → ml-texto:8001               ✅ Funciona
n8n → ml-imagen:8002              ✅ Funciona
n8n → postgres:5432               ✅ Funciona
```

---

## 🚀 **Comandos Principales**

### **Iniciar solo n8n:**
```bash
# Windows
start-n8n.bat

# Linux/Mac
./start-n8n.sh

# Manual
docker-compose --env-file .env.n8n -f docker-compose.n8n.yml up -d
```

### **Iniciar TODO (App + n8n):**
```bash
# Windows
start-all.bat

# Linux/Mac
./start-all.sh
```

### **Verificar estado:**
```bash
docker-compose -f docker-compose.n8n.yml ps
```

### **Ver logs:**
```bash
docker-compose -f docker-compose.n8n.yml logs -f
```

### **Detener:**
```bash
# Solo n8n
docker-compose -f docker-compose.n8n.yml down

# Todo
./stop-all.sh  # o stop-all.bat en Windows
```

---

## 🔑 **Acceso a n8n**

### **URL:**
http://localhost:5678

### **Credenciales por defecto:**
- Usuario: `admin`
- Contraseña: `admin123`

⚠️ **IMPORTANTE:** Cambiar en producción editando `.env.n8n`:
```env
N8N_BASIC_AUTH_USER=tu_usuario_seguro
N8N_BASIC_AUTH_PASSWORD=contraseña_muy_segura
```

---

## 📊 **Volúmenes Persistentes**

### **n8n_data:**
```
Nombre: recepcion_n8n_data
Contenido:
  - database.sqlite (workflows, credenciales, ejecuciones)
  - .n8n/ (configuración)
```

### **Backup:**
```bash
docker run --rm \
  -v recepcion_n8n_data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/n8n-backup.tar.gz /data
```

---

## 🤖 **Bots a Implementar**

### **1. Bot de WhatsApp**
- **Plataforma:** WhatsApp Business Cloud API
- **Workflow:** `n8n/workflows/bot-whatsapp.json` (pendiente)
- **Trigger:** Webhook de WhatsApp
- **Funcionalidad:**
  - Recibir mensajes
  - Solicitar datos (nombre, descripción, ubicación)
  - Crear incidente en microservicio
  - Responder con código y prioridad

### **2. Bot de Telegram**
- **Plataforma:** Telegram Bot API
- **Workflow:** `n8n/workflows/bot-telegram.json` (pendiente)
- **Trigger:** Comandos de Telegram
- **Comandos:**
  - `/start` - Iniciar bot
  - `/solicitar` - Solicitar ambulancia
  - `/ayuda` - Ayuda
- **Funcionalidad:**
  - Conversación guiada
  - Botones inline
  - Compartir ubicación GPS
  - Crear incidente
  - Confirmación

---

## 🔧 **Configuración Pendiente**

### **Paso 1: Obtener Credenciales**

#### WhatsApp:
1. Crear app en Meta for Developers
2. Obtener Phone Number ID
3. Generar Access Token permanente
4. Configurar Verify Token

#### Telegram:
1. Hablar con @BotFather en Telegram
2. Ejecutar `/newbot`
3. Obtener Bot Token

Ver: [n8n/credentials/README.md](n8n/credentials/README.md)

### **Paso 2: Configurar en n8n UI**
1. Abrir http://localhost:5678
2. Settings → Credentials
3. Agregar credenciales de WhatsApp
4. Agregar credenciales de Telegram

### **Paso 3: Crear Workflows**
1. Crear workflows en n8n UI
2. Conectar nodos (Webhook → Process → HTTP Request → Response)
3. Probar con datos de ejemplo
4. Exportar workflows a `n8n/workflows/`

Ver: [n8n/workflows/README.md](n8n/workflows/README.md)

### **Paso 4: Configurar Webhooks**

**Desarrollo local:**
```bash
# Usar ngrok para exponer puerto 5678
ngrok http 5678

# Actualizar .env.n8n con URL de ngrok
WEBHOOK_URL=https://abc123.ngrok.io
```

**Producción:**
```env
WEBHOOK_URL=https://tu-dominio.com
```

---

## 📈 **Flujo End-to-End Completo**

```
1. Usuario envía mensaje por WhatsApp/Telegram
   "Necesito una ambulancia urgente"
   ↓
2. Webhook dispara workflow en n8n
   ↓
3. n8n extrae información del mensaje
   - Nombre: (de perfil de usuario)
   - Teléfono: (de perfil)
   - Descripción: "Necesito una ambulancia urgente"
   ↓
4. n8n solicita más datos al usuario
   "Por favor, comparte tu ubicación"
   ↓
5. Usuario comparte ubicación GPS
   Latitud: -17.3935
   Longitud: -66.1570
   ↓
6. n8n hace HTTP Request al microservicio
   POST http://microservicio:8080/api/incidentes
   Headers: X-API-Key: dev-key-12345
   Body:
   {
     "solicitante": {
       "nombre": "Usuario WhatsApp",
       "telefono": "+59170123456"
     },
     "ubicacion": {
       "latitud": -17.3935,
       "longitud": -66.1570,
       "direccion": "Av. Heroínas, Cochabamba"
     },
     "descripcion": "Necesito una ambulancia urgente",
     "tipo": "EMERGENCIA_MEDICA"
   }
   ↓
7. Microservicio procesa:
   - Guarda en PostgreSQL
   - Lanza análisis ML de texto (asíncrono)
   - Retorna respuesta inmediata:
   {
     "id": 1,
     "codigo": "INC-20250107-0001",
     "estado": "PENDIENTE",
     ...
   }
   ↓
8. n8n responde al usuario:
   "✅ Solicitud registrada exitosamente!

   📋 Código: INC-20250107-0001
   🚑 Estado: PENDIENTE
   ⏰ Prioridad: Se está calculando...

   La ambulancia está en camino.
   Te mantendremos informado."
   ↓
9. ML Texto analiza (en background):
   - Prioridad: ALTA (4/5)
   - Keywords: ["ambulancia", "urgente"]
   - Score: 0.85
   ↓
10. Microservicio actualiza incidente:
    - prioridadTexto: 4
    - prioridadFinal: 4
    - estado: ANALIZADO
    ↓
11. (Opcional) n8n puede hacer polling o webhook
    para notificar al usuario cuando cambie el estado
```

---

## 🎯 **Estado del Proyecto**

### **Completado ✅**
- [x] Dockerización de n8n
- [x] Red compartida entre stacks
- [x] Scripts de gestión
- [x] Documentación completa
- [x] Estructura de carpetas
- [x] Variables de entorno

### **Pendiente 🔄**
- [ ] Obtener credenciales de WhatsApp Business API
- [ ] Obtener credenciales de Telegram Bot
- [ ] Configurar credenciales en n8n UI
- [ ] Crear workflow de WhatsApp
- [ ] Crear workflow de Telegram
- [ ] Configurar webhooks
- [ ] Probar flujo completo end-to-end
- [ ] Deploy a producción con dominio público

---

## 📚 **Guías de Referencia**

- **Inicio Rápido:** [N8N_README.md](N8N_README.md)
- **Guía Completa:** [n8n/README.md](n8n/README.md)
- **Credenciales:** [n8n/credentials/README.md](n8n/credentials/README.md)
- **Workflows:** [n8n/workflows/README.md](n8n/workflows/README.md)
- **Docker App:** [DOCKER_README.md](DOCKER_README.md)
- **Docker Guide:** [DOCKER_GUIDE.md](DOCKER_GUIDE.md)

---

## 🆘 **Troubleshooting**

### n8n no puede conectarse al microservicio
```bash
# Verificar que ambos están en la misma red
docker network inspect recepcion-network

# Debe mostrar ambos contenedores
```

### Webhooks no funcionan en desarrollo
```bash
# Usa ngrok
ngrok http 5678

# Actualiza WEBHOOK_URL en .env.n8n
```

### Credenciales no se guardan
```bash
# Verificar volumen
docker volume inspect recepcion_n8n_data

# Verificar permisos
docker exec -it recepcion-n8n ls -la /home/node/.n8n
```

---

## 🎓 **Recursos Adicionales**

- [Documentación oficial de n8n](https://docs.n8n.io/)
- [n8n Community](https://community.n8n.io/)
- [n8n Workflow Templates](https://n8n.io/workflows)
- [WhatsApp Business API](https://developers.facebook.com/docs/whatsapp/cloud-api/)
- [Telegram Bot API](https://core.telegram.org/bots/api)

---

**Fecha de creación:** 2025-01-07
**Estado:** ✅ n8n dockerizado y listo para configurar bots
**Próximo paso:** Obtener credenciales y crear workflows

---

**Total de archivos creados:** 17
**Documentación:** 6 archivos MD
**Scripts:** 10 archivos (bat + sh)
**Configuración:** Docker Compose + .env
