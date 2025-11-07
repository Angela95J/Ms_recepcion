# 🤖 n8n - Inicio Rápido

Bot de WhatsApp/Telegram para solicitud de ambulancias usando n8n.

---

## ⚡ Inicio Rápido (5 minutos)

### Windows
```bash
# 1. Iniciar Docker Desktop

# 2. Ejecutar el script de inicio
start-n8n.bat
```

### Linux/Mac
```bash
# 1. Dar permisos (solo primera vez)
chmod +x *.sh

# 2. Ejecutar el script de inicio
./start-n8n.sh
```

### Acceder a n8n

Abre tu navegador en: **http://localhost:5678**

**Credenciales por defecto:**
- Usuario: `admin`
- Contraseña: `admin123`

⚠️ **Cambiar en producción** editando `.env.n8n`

---

## 📋 Comandos Esenciales

```bash
# Ver logs en tiempo real
docker-compose -f docker-compose.n8n.yml logs -f

# Detener n8n
docker-compose -f docker-compose.n8n.yml down

# Reiniciar n8n
docker-compose -f docker-compose.n8n.yml restart
```

---

## 🏗️ Arquitectura

```
┌──────────────────────────────────────────────┐
│         Docker Network (recepcion-network)   │
├──────────────────────────────────────────────┤
│                                              │
│  ┌──────────────┐      ┌─────────────────┐  │
│  │     n8n      │◄─────┤  Microservicio  │  │
│  │  (Puerto     │      │   Spring Boot   │  │
│  │   5678)      │      │  (Puerto 8080)  │  │
│  └──────┬───────┘      └─────────────────┘  │
│         │                                    │
│         │ Webhooks                           │
│         │                                    │
└─────────┼────────────────────────────────────┘
          │
          ▼
  ┌──────────────────┐
  │  WhatsApp/       │
  │  Telegram        │
  │  (Usuarios)      │
  └──────────────────┘
```

**Flujo:**
1. Usuario envía mensaje por WhatsApp/Telegram
2. Webhook dispara workflow en n8n
3. n8n procesa el mensaje y llama al microservicio
4. Microservicio crea el incidente con análisis ML
5. n8n responde al usuario con confirmación

---

## 🔧 Configuración Inicial

### Paso 1: Configurar Credenciales

Ver guía detallada: [n8n/credentials/README.md](n8n/credentials/README.md)

**Necesitas:**
- **WhatsApp:** Phone Number ID + Access Token
- **Telegram:** Bot Token (de @BotFather)

**Cómo configurar:**
1. Abre n8n: http://localhost:5678
2. Ve a: Settings → Credentials
3. Agrega las credenciales según la guía

### Paso 2: Crear Workflows

Ver ejemplos: [n8n/workflows/README.md](n8n/workflows/README.md)

**Workflows recomendados:**
- `bot-whatsapp.json` - Bot de WhatsApp
- `bot-telegram.json` - Bot de Telegram

### Paso 3: Configurar Webhooks

Para desarrollo local, usa **ngrok** para exponer el puerto:

```bash
# Instalar ngrok
# https://ngrok.com/download

# Exponer puerto 5678
ngrok http 5678

# Usar la URL de ngrok como Webhook URL
# Ejemplo: https://abc123.ngrok.io
```

Actualiza `.env.n8n`:
```env
WEBHOOK_URL=https://abc123.ngrok.io
```

---

## 🎯 Ejemplo de Workflow

### Bot de Telegram - Solicitar Ambulancia

```
1. Usuario: /solicitar
   ↓
2. Bot: "Por favor, comparte tu nombre"
   ↓
3. Usuario: "Juan Pérez"
   ↓
4. Bot: "Describe la emergencia"
   ↓
5. Usuario: "Accidente de tránsito, herido grave"
   ↓
6. Bot: "Comparte tu ubicación"
   ↓
7. Usuario: [Envía ubicación GPS]
   ↓
8. n8n → POST /api/incidentes (microservicio)
   ↓
9. Bot: "✅ Solicitud registrada!
          Código: INC-20250107-0001
          Prioridad: ALTA
          La ambulancia está en camino."
```

---

## 🔗 Integración con Microservicio

n8n se comunica con el microservicio usando variables de entorno:

```yaml
# En .env.n8n
MICROSERVICIO_BASE_URL=http://microservicio:8080
MICROSERVICIO_API_KEY=dev-key-12345
```

**En workflow (HTTP Request Node):**
```
URL: {{ $env.MICROSERVICIO_BASE_URL }}/api/incidentes
Headers:
  X-API-Key: {{ $env.MICROSERVICIO_API_KEY }}
Method: POST
Body:
{
  "solicitante": { ... },
  "ubicacion": { ... },
  "descripcion": "...",
  "tipo": "EMERGENCIA_MEDICA"
}
```

---

## 📊 Volúmenes Persistentes

```
n8n_data:
  - Base de datos SQLite (workflows, credenciales, ejecuciones)
  - Ubicación: /home/node/.n8n/database.sqlite
```

**Backup:**
```bash
# Crear backup del volumen
docker run --rm \
  -v recepcion_n8n_data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/n8n-backup.tar.gz /data

# Restaurar backup
docker run --rm \
  -v recepcion_n8n_data:/data \
  -v $(pwd):/backup \
  alpine tar xzf /backup/n8n-backup.tar.gz -C /
```

---

## 🐛 Problemas Comunes

### n8n no inicia
```bash
# Ver logs
docker-compose -f docker-compose.n8n.yml logs

# Verificar red
docker network ls | grep recepcion-network

# Si no existe la red
docker network create recepcion-network
```

### No puede conectarse al microservicio
```bash
# Verificar que ambos están en la misma red
docker inspect recepcion-n8n | grep Networks
docker inspect recepcion-microservicio | grep Networks

# Probar conexión
docker exec recepcion-n8n ping -c 3 microservicio
```

### Webhooks no funcionan (desarrollo local)
1. Usa **ngrok**: `ngrok http 5678`
2. Actualiza `WEBHOOK_URL` en `.env.n8n` con la URL de ngrok
3. Reinicia n8n: `docker-compose -f docker-compose.n8n.yml restart`

---

## 🚀 Iniciar TODO el Sistema

Para iniciar ambos stacks (Microservicio + n8n):

```bash
# Windows
start-all.bat

# Linux/Mac
./start-all.sh
```

Esto iniciará:
- PostgreSQL
- Microservicio Spring Boot
- ML Texto
- ML Imagen
- n8n

---

## 📚 Documentación Completa

- **Guía de n8n:** [n8n/README.md](n8n/README.md)
- **Credenciales:** [n8n/credentials/README.md](n8n/credentials/README.md)
- **Workflows:** [n8n/workflows/README.md](n8n/workflows/README.md)
- **Docker Guide:** [DOCKER_GUIDE.md](DOCKER_GUIDE.md)

---

## 🔐 Seguridad en Producción

1. **Cambiar credenciales:**
   ```env
   # .env.n8n
   N8N_BASIC_AUTH_USER=tu_usuario_seguro
   N8N_BASIC_AUTH_PASSWORD=contraseña_muy_segura_123
   ```

2. **Usar HTTPS:**
   ```env
   N8N_PROTOCOL=https
   WEBHOOK_URL=https://tu-dominio.com
   ```

3. **Configurar dominio público:**
   - Usar nginx o traefik como reverse proxy
   - Configurar SSL/TLS con Let's Encrypt

4. **Restringir acceso:**
   - Firewall para limitar acceso a n8n
   - Solo exponer webhooks públicamente

---

## 📈 Próximos Pasos

1. ✅ n8n dockerizado e iniciado
2. 🔄 Configurar credenciales de WhatsApp/Telegram
3. 🔄 Crear workflows de bots
4. 🔄 Probar flujo completo end-to-end
5. 🔄 Deploy a producción con HTTPS

---

**¿Listo para producción?** Revisa [DOCKER_GUIDE.md - Producción](DOCKER_GUIDE.md#producción)

**Última actualización:** 2025-01-07
