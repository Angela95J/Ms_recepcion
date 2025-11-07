# 🔐 Configuración de Credenciales en n8n

Esta guía explica cómo configurar las credenciales necesarias para los bots de WhatsApp y Telegram en n8n.

---

## ⚠️ IMPORTANTE

**Las credenciales NO se guardan en este repositorio por seguridad.**

Las credenciales se almacenan en:
- El volumen Docker `recepcion_n8n_data`
- Encriptadas dentro de n8n

Esta carpeta solo contiene documentación de cómo configurarlas.

---

## 📱 WhatsApp Business Cloud API

### Prerequisitos
1. Cuenta de Meta for Developers
2. Aplicación de WhatsApp Business
3. Número de teléfono verificado

### Pasos para obtener credenciales

#### 1. Crear App en Meta for Developers
1. Ve a: https://developers.facebook.com/
2. Clic en "My Apps" → "Create App"
3. Selecciona tipo: "Business"
4. Llena los datos de tu app
5. En el dashboard, agrega el producto "WhatsApp"

#### 2. Obtener Phone Number ID y Access Token
1. En el dashboard de WhatsApp, ve a "API Setup"
2. Copia el **Phone Number ID** (empieza con números)
3. Copia el **Temporary Access Token** (empieza con `EAA...`)
   - ⚠️ Este token es temporal, genera uno permanente después
4. Para token permanente:
   - Ve a "Settings" → "System Users"
   - Crea un System User
   - Genera un token con permisos de WhatsApp

#### 3. Configurar Webhook Verify Token
1. Elige un token de verificación (string aleatorio)
   - Ejemplo: `mi-token-super-secreto-12345`
2. Guárdalo, lo necesitarás al configurar el webhook

### Configurar en n8n

1. Abre n8n: http://localhost:5678
2. Ve a: **Settings → Credentials** (o haz clic en el ícono de llave)
3. Clic en **"+ Add Credential"**
4. Busca y selecciona: **"WhatsApp Business Cloud API"**
5. Completa los campos:
   ```
   Name: WhatsApp Bot Ambulancias
   Phone Number ID: [tu Phone Number ID]
   Access Token: [tu Access Token permanente]
   ```
6. Clic en **"Save"**

### Configurar Webhook en Meta

1. En el dashboard de WhatsApp, ve a "Configuration" → "Webhook"
2. Clic en "Edit"
3. Ingresa:
   ```
   Callback URL: https://TU-DOMINIO/webhook/whatsapp
   Verify Token: [el token que elegiste en el paso 3]
   ```
4. Suscríbete a los eventos:
   - `messages`
   - `message_status`

⚠️ **Nota para desarrollo local:**
- Usa **ngrok** para exponer tu puerto 5678:
  ```bash
  ngrok http 5678
  ```
- Usa la URL de ngrok como Callback URL

---

## 🤖 Telegram Bot

### Prerequisitos
- Cuenta de Telegram

### Pasos para obtener credenciales

#### 1. Crear Bot con BotFather

1. Abre Telegram y busca: **@BotFather**
2. Envía el comando: `/newbot`
3. BotFather te pedirá:
   - **Nombre del bot** (ej: "Ambulancias Bolivia Bot")
   - **Username del bot** (debe terminar en `bot`, ej: `ambulancias_bol_bot`)
4. BotFather te dará el **Bot Token**:
   ```
   Use this token to access the HTTP API:
   1234567890:ABCdefGHIjklMNOpqrsTUVwxyz1234567
   ```
5. ⚠️ **Guarda este token de forma segura**

#### 2. Configurar el Bot (opcional)

```
/setdescription - Establecer descripción del bot
/setabouttext - Texto "Acerca de"
/setuserpic - Foto de perfil

Ejemplo:
/setdescription
Bot oficial para solicitar ambulancias en Bolivia.
Envía /solicitar para comenzar.
```

#### 3. Obtener tu Chat ID (para testing)

1. Busca: **@userinfobot** en Telegram
2. Envía `/start`
3. El bot te dará tu **Chat ID** (número)

### Configurar en n8n

1. Abre n8n: http://localhost:5678
2. Ve a: **Settings → Credentials**
3. Clic en **"+ Add Credential"**
4. Busca y selecciona: **"Telegram"**
5. Completa los campos:
   ```
   Name: Telegram Bot Ambulancias
   Access Token: [tu Bot Token de BotFather]
   ```
6. Clic en **"Save"**

### Configurar Webhook (automático en n8n)

n8n configura automáticamente el webhook de Telegram cuando activas el workflow.

La URL será: `https://TU-DOMINIO/webhook/telegram`

Para verificar que el webhook está configurado:
```bash
curl https://api.telegram.org/bot<TU-BOT-TOKEN>/getWebhookInfo
```

---

## 🔑 Resumen de Credenciales Necesarias

### WhatsApp
| Campo | Descripción | Ejemplo |
|-------|-------------|---------|
| Phone Number ID | ID del número de WhatsApp | `102345678901234` |
| Access Token | Token de acceso permanente | `EAA...` (muy largo) |
| Verify Token | Token para verificar webhook | `mi-token-secreto-12345` |

### Telegram
| Campo | Descripción | Ejemplo |
|-------|-------------|---------|
| Bot Token | Token del bot de BotFather | `1234567890:ABCdef...` |

---

## 🧪 Testing de Credenciales

### WhatsApp
```bash
# Test del token
curl -X GET "https://graph.facebook.com/v18.0/me/messages?access_token=<TU_TOKEN>"

# Debe retornar: {"data":[...]}
```

### Telegram
```bash
# Test del bot
curl https://api.telegram.org/bot<TU_BOT_TOKEN>/getMe

# Debe retornar info del bot
```

---

## 🔒 Seguridad

### ✅ Buenas Prácticas

1. **Nunca commitar credenciales al repositorio**
   - Las credenciales se guardan solo en n8n
   - Este README solo documenta el proceso

2. **Rotar tokens periódicamente**
   - WhatsApp: Regenera el Access Token cada 90 días
   - Telegram: Revoca y crea nuevo bot si el token se compromete

3. **Usar tokens con permisos mínimos**
   - Solo los permisos necesarios para enviar/recibir mensajes

4. **En producción:**
   - Usa HTTPS
   - Restringe acceso a n8n por IP
   - Habilita autenticación fuerte

---

## 🆘 Troubleshooting

### WhatsApp: "Invalid Phone Number ID"
- Verifica que copiaste el ID correcto (solo números)
- Asegúrate de que el número esté verificado en Meta

### WhatsApp: "Invalid Access Token"
- El token temporal expira en 24 horas
- Genera un token permanente con System User

### Telegram: "Unauthorized"
- Verifica que el Bot Token sea correcto
- Revisa que no tenga espacios al inicio/final

### Webhook no funciona en desarrollo local
- Usa ngrok: `ngrok http 5678`
- Actualiza el Callback URL con la URL de ngrok
- Verifica que el puerto 5678 esté abierto

---

## 📚 Referencias

- [WhatsApp Cloud API Docs](https://developers.facebook.com/docs/whatsapp/cloud-api/)
- [Telegram Bot API](https://core.telegram.org/bots/api)
- [BotFather Commands](https://core.telegram.org/bots/features#botfather)
- [n8n Credentials](https://docs.n8n.io/credentials/)

---

**Última actualización:** 2025-01-07
