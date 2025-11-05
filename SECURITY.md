# 🔒 Guía de Seguridad - API Keys

## Descripción General

El microservicio utiliza un sistema simple de **API Keys** para autenticación. No requiere login de usuarios, solo claves API en los headers de las peticiones.

## 🔑 Tipos de API Keys

### 1. API Key ADMIN (Administrador)

**Uso:** Panel web de administración

**Permisos:**
- ✅ Crear, leer, actualizar, eliminar incidentes
- ✅ Aprobar/Rechazar incidentes
- ✅ Gestionar solicitantes y ubicaciones
- ✅ Ver análisis ML
- ✅ Subir/eliminar multimedia
- ✅ Acceso COMPLETO a todos los endpoints

**Valor por defecto:** `admin-key-change-in-production-12345`

### 2. API Key N8N (Automatización)

**Uso:** Bots de WhatsApp/Telegram vía n8n

**Permisos:**
- ✅ Crear incidentes (POST /api/incidentes)
- ✅ Crear solicitantes (POST /api/solicitantes)
- ✅ Crear ubicaciones (POST /api/ubicaciones)
- ✅ Subir multimedia (POST /api/multimedia/incidente/{id}/subir)
- ✅ Ver multimedia subido (GET /api/multimedia/{id})
- ❌ NO puede aprobar/rechazar
- ❌ NO puede eliminar
- ❌ NO puede actualizar (PUT/PATCH)

**Valor por defecto:** `n8n-key-change-in-production-67890`

## 🔧 Configuración

### Variables de Entorno (Recomendado)

```bash
# En producción, usar variables de entorno
export API_KEY_ADMIN="tu-clave-admin-super-secreta"
export API_KEY_N8N="tu-clave-n8n-super-secreta"
```

### Archivo application.yml

```yaml
api:
  keys:
    admin: ${API_KEY_ADMIN:admin-key-change-in-production-12345}
    n8n: ${API_KEY_N8N:n8n-key-change-in-production-67890}
```

## 📡 Uso de API Keys

### Header Requerido

Todas las peticiones (excepto rutas públicas) deben incluir el header:

```
X-API-KEY: <tu-api-key>
```

### Ejemplo con cURL

```bash
# Con API Key de ADMIN
curl -X GET "http://localhost:8080/api/incidentes" \
  -H "X-API-KEY: admin-key-change-in-production-12345"

# Con API Key de N8N
curl -X POST "http://localhost:8080/api/incidentes" \
  -H "X-API-KEY: n8n-key-change-in-production-67890" \
  -H "Content-Type: application/json" \
  -d '{
    "solicitante": {...},
    "ubicacion": {...},
    "descripcionOriginal": "Accidente de tránsito"
  }'
```

### Ejemplo con Postman

1. Abrir Postman
2. Ir a la pestaña **Headers**
3. Agregar:
   - **Key:** `X-API-KEY`
   - **Value:** `admin-key-change-in-production-12345`

### Ejemplo con JavaScript/Fetch

```javascript
fetch('http://localhost:8080/api/incidentes', {
  method: 'GET',
  headers: {
    'X-API-KEY': 'admin-key-change-in-production-12345',
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

### Ejemplo con Python/Requests

```python
import requests

headers = {
    'X-API-KEY': 'admin-key-change-in-production-12345',
    'Content-Type': 'application/json'
}

response = requests.get(
    'http://localhost:8080/api/incidentes',
    headers=headers
)

print(response.json())
```

## 🤖 Integración con n8n

### Configurar API Key en n8n

En cada nodo **HTTP Request** que llame al microservicio:

**Headers:**
```
X-API-KEY: n8n-key-change-in-production-67890
```

### Ejemplo de Workflow n8n

```json
{
  "nodes": [
    {
      "name": "Crear Incidente",
      "type": "n8n-nodes-base.httpRequest",
      "parameters": {
        "url": "http://localhost:8080/api/incidentes",
        "method": "POST",
        "headerParameters": {
          "parameters": [
            {
              "name": "X-API-KEY",
              "value": "n8n-key-change-in-production-67890"
            }
          ]
        },
        "bodyParameters": {
          "solicitante": {
            "nombreCompleto": "={{ $json.from.name }}",
            "telefono": "={{ $json.from.phone }}",
            "canalOrigen": "WHATSAPP"
          },
          "ubicacion": {
            "descripcionTextual": "={{ $json.location.address }}"
          },
          "descripcionOriginal": "={{ $json.message }}"
        }
      }
    }
  ]
}
```

## 🛡️ Respuestas de Error

### Sin API Key (401 Unauthorized)

```json
{
  "error": "API Key requerida. Use header X-API-KEY"
}
```

### API Key Inválida (403 Forbidden)

```json
{
  "error": "API Key inválida"
}
```

### Permiso Denegado (403 Forbidden)

Cuando n8n intenta acceder a endpoints no permitidos:

```json
{
  "error": "Acceso no permitido con esta API Key"
}
```

## 🔍 Permisos Detallados por Endpoint

### Endpoints Públicos (Sin API Key)

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/actuator/**` | GET | Health check y métricas |

### Endpoints con API Key N8N

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/incidentes` | POST | Crear incidente |
| `/api/solicitantes` | POST | Crear solicitante |
| `/api/ubicaciones` | POST | Crear ubicación |
| `/api/multimedia/incidente/{id}/subir` | POST | Subir archivo |
| `/api/multimedia/{id}` | GET | Ver metadata |
| `/api/multimedia/{id}/ver` | GET | Ver archivo |

### Endpoints Solo con API Key ADMIN

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/incidentes/**` | GET, PUT, PATCH, DELETE | Gestión completa |
| `/api/incidentes/{id}/aprobar` | POST | Aprobar incidente |
| `/api/incidentes/{id}/rechazar` | POST | Rechazar incidente |
| `/api/solicitantes/**` | GET, PUT, DELETE | Gestión completa |
| `/api/ubicaciones/**` | GET, PUT, DELETE | Gestión completa |
| `/api/multimedia/**` | DELETE | Eliminar archivos |
| `/api/analisis-texto/**` | GET | Ver análisis ML |
| `/api/analisis-imagen/**` | GET | Ver análisis ML |
| `/api/historial-estados/**` | GET | Ver historial |

## 🔐 Mejores Prácticas de Seguridad

### En Desarrollo

✅ Usar las claves por defecto está bien
✅ Guardar las claves en `.env` (no versionar)

### En Producción

❗ **OBLIGATORIO:**
1. **Cambiar las API Keys** - Generar claves aleatorias largas
2. **Usar variables de entorno** - Nunca hardcodear en código
3. **HTTPS obligatorio** - Nunca usar HTTP en producción
4. **Rotar claves periódicamente** - Cada 90 días mínimo
5. **Logs de auditoría** - Monitorear accesos sospechosos

### Generar API Keys Seguras

```bash
# Linux/Mac
openssl rand -hex 32

# Python
python -c "import secrets; print(secrets.token_urlsafe(32))"

# Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```

### Ejemplo de Producción

```bash
# .env (NO versionar este archivo)
API_KEY_ADMIN=8f4c7e2a9d6b1f3a5e8c2d7b4a9e6f1c3d8a5b2e7f4c9a1d6b3e8f5a2c7d4b9e
API_KEY_N8N=3a7e9c2f5d8b1a4e7c2d5b8a3e6f9c2d5a8b1e4f7c9a2d5b8e1f4a7c3d6b9e2
```

```yaml
# application.yml
api:
  keys:
    admin: ${API_KEY_ADMIN}
    n8n: ${API_KEY_N8N}
```

## 🚨 Limitaciones

Esta implementación simple de API Keys tiene las siguientes limitaciones:

1. **No hay expiración** - Las claves no expiran automáticamente
2. **No hay rate limiting** - Sin límite de peticiones por segundo
3. **No hay revocación dinámica** - Requiere reinicio para cambiar claves
4. **No hay múltiples admins** - Solo una clave admin

Para producción con múltiples administradores, considera migrar a JWT con tabla de usuarios.

## 📊 Monitoreo

Los logs incluyen información de autenticación:

```
# Petición exitosa
DEBUG - Petición autenticada con API Key tipo: ADMIN

# Petición sin API Key
WARN - Petición sin API Key a: POST /api/incidentes

# API Key inválida
WARN - API Key inválida en petición a: GET /api/incidentes

# Permiso denegado
WARN - API Key de n8n intentando acceder a ruta no permitida: DELETE /api/incidentes/123
```

## ❓ FAQ

**P: ¿Por qué no usar JWT?**
R: Los solicitantes reportan desde WhatsApp/Telegram sin login. Solo el admin necesita autenticación, y API Keys es más simple.

**P: ¿Es seguro para producción?**
R: Sí, si usas HTTPS y claves largas aleatorias. Para múltiples admins, considera JWT.

**P: ¿Cómo cambio las API Keys?**
R: Cambia las variables de entorno y reinicia la aplicación.

**P: ¿Puedo tener múltiples API Keys para n8n?**
R: Actualmente solo una. Para múltiples, modifica `ApiKeyFilter.java` para aceptar lista de claves.

**P: ¿Las API Keys se almacenan en base de datos?**
R: No, están en configuración (application.yml / variables de entorno).

---

**🔗 Ver también:**
- [README.md](./README.md) - Documentación general
- [MULTIMEDIA_STORAGE.md](./MULTIMEDIA_STORAGE.md) - Almacenamiento de archivos
