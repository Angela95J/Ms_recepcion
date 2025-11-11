# Verificación de Conexión n8n ↔ Microservicio

## Resumen

Este documento explica cómo verificar que n8n se puede conectar correctamente con el microservicio de recepción.

---

## Configuración Correcta

### 1. Red Docker

Ambos servicios deben estar en la **misma red Docker**:

```yaml
# docker-compose.app.yml
networks:
  recepcion-network:
    driver: bridge
    name: recepcion-network

# docker-compose.n8n.yml
networks:
  recepcion-network:
    external: true  # Usa la red creada por docker-compose.app.yml
    name: recepcion-network
```

✅ **Verificado**: Ambos están en `recepcion-network`

### 2. Nombre del Servicio

En Docker Compose, los servicios se comunican usando sus **nombres de servicio**:

```yaml
# docker-compose.app.yml
services:
  microservicio:  # ← Este es el nombre que n8n debe usar
    container_name: recepcion-microservicio
    ports:
      - "8080:8080"
    networks:
      - recepcion-network
```

✅ **Nombre correcto**: `microservicio`

### 3. Context Path de Spring Boot

En `application.yml`:

```yaml
server:
  port: 8080
  servlet:
    context-path: /api  # ← TODOS los endpoints tienen prefijo /api
```

✅ **Prefijo correcto**: `/api`

---

## URLs Correctas en n8n

### Desde DENTRO de Docker (n8n → microservicio)

```
✅ POST http://microservicio:8080/api/incidentes
✅ GET  http://microservicio:8080/api/incidentes/{id}/detalle
```

**Explicación**:
- `microservicio` → Nombre del servicio en docker-compose.app.yml
- `8080` → Puerto interno del contenedor
- `/api` → Context path de Spring Boot
- `/incidentes` → Ruta del controlador (`@RequestMapping("/incidentes")`)

### Desde FUERA de Docker (Postman, navegador)

```
✅ POST http://localhost:8080/api/incidentes
✅ GET  http://localhost:8080/api/incidentes/{id}/detalle
```

**Explicación**:
- `localhost` → Acceso desde tu máquina host
- `8080` → Puerto mapeado en `ports: - "8080:8080"`
- `/api/incidentes` → Igual que antes

---

## Verificación Paso a Paso

### 1. Verificar que los servicios están corriendo

```bash
# Ver contenedores activos
docker ps

# Deberías ver:
# - recepcion-microservicio
# - recepcion-n8n
# - recepcion-postgres
# - recepcion-ml-texto
# - recepcion-ml-imagen
```

### 2. Verificar la red Docker

```bash
# Inspeccionar la red
docker network inspect recepcion-network

# Deberías ver todos los servicios conectados a esta red
```

### 3. Probar conexión desde tu máquina (FUERA de Docker)

```bash
# Probar endpoint de salud (si existe)
curl http://localhost:8080/api/actuator/health

# O probar listar incidentes
curl -H "X-API-Key: dev-key-12345" http://localhost:8080/api/incidentes
```

### 4. Probar conexión DENTRO de Docker (desde n8n)

```bash
# Entrar al contenedor de n8n
docker exec -it recepcion-n8n sh

# Probar conexión al microservicio
wget -qO- http://microservicio:8080/api/incidentes \
  --header="X-API-Key: dev-key-12345"

# Si funciona, deberías ver una respuesta JSON
```

### 5. Verificar en el workflow de n8n

En n8n, crea un nodo de prueba HTTP Request:

```json
{
  "method": "GET",
  "url": "http://microservicio:8080/api/incidentes",
  "headers": {
    "X-API-Key": "dev-key-12345"
  }
}
```

Si funciona, verás la lista de incidentes.

---

## Problemas Comunes y Soluciones

### ❌ Error: "Could not resolve host: recepcion-app"

**Problema**: Estás usando el nombre incorrecto del servicio.

**Solución**:
```diff
- http://recepcion-app:8080/api/incidentes
+ http://microservicio:8080/api/incidentes
```

### ❌ Error: "404 Not Found"

**Problema**: Falta el prefijo `/api` o la ruta es incorrecta.

**Solución**:
```diff
- http://microservicio:8080/incidentes
+ http://microservicio:8080/api/incidentes
```

### ❌ Error: "Connection refused"

**Causas posibles**:
1. El microservicio no está corriendo
2. Los servicios no están en la misma red
3. El puerto está bloqueado

**Solución**:
```bash
# 1. Verificar que el microservicio está corriendo
docker ps | grep microservicio

# 2. Verificar logs del microservicio
docker logs recepcion-microservicio

# 3. Verificar que ambos están en la misma red
docker network inspect recepcion-network
```

### ❌ Error: "401 Unauthorized" o "403 Forbidden"

**Problema**: Falta el API Key o es incorrecto.

**Solución**:
```json
{
  "headers": {
    "X-API-Key": "dev-key-12345"
  }
}
```

Verificar en `.env`:
```env
API_KEY_N8N=dev-key-12345
```

---

## Configuración del Workflow

El workflow `bot-telegram-CORREGIDO.json` ya tiene las URLs correctas:

### Nodo: "POST /incidentes"

```json
{
  "method": "POST",
  "url": "http://microservicio:8080/api/incidentes",
  "headers": {
    "X-API-Key": "dev-key-12345",
    "Content-Type": "application/json"
  },
  "body": {
    "solicitante": {...},
    "ubicacion": {...},
    "descripcionOriginal": "..."
  }
}
```

### Nodo: "GET /incidentes/{id}/detalle"

```json
{
  "method": "GET",
  "url": "http://microservicio:8080/api/incidentes/{{ $('POST /incidentes').first().json.id }}/detalle",
  "headers": {
    "X-API-Key": "dev-key-12345"
  }
}
```

---

## Tabla de Referencia Rápida

| Contexto | URL | Explicación |
|----------|-----|-------------|
| **Desde n8n** (dentro de Docker) | `http://microservicio:8080/api/incidentes` | Usa nombre del servicio |
| **Desde tu PC** (fuera de Docker) | `http://localhost:8080/api/incidentes` | Usa localhost + puerto mapeado |
| **Desde otro contenedor** | `http://microservicio:8080/api/incidentes` | Usa nombre del servicio |

---

## Comandos Útiles

```bash
# Ver logs del microservicio
docker logs -f recepcion-microservicio

# Ver logs de n8n
docker logs -f recepcion-n8n

# Reiniciar solo el microservicio
docker restart recepcion-microservicio

# Reiniciar solo n8n
docker restart recepcion-n8n

# Verificar variables de entorno del microservicio
docker exec recepcion-microservicio env | grep API_KEY

# Probar endpoint desde el contenedor de n8n
docker exec -it recepcion-n8n sh
wget -qO- http://microservicio:8080/api/incidentes \
  --header="X-API-Key: dev-key-12345"
```

---

## Checklist Final

Antes de usar el workflow, verifica:

- [ ] Todos los servicios están corriendo (`docker ps`)
- [ ] Ambos servicios están en `recepcion-network` (`docker network inspect recepcion-network`)
- [ ] El microservicio responde en `http://localhost:8080/api/incidentes` (desde tu PC)
- [ ] n8n puede acceder al microservicio usando `http://microservicio:8080/api/incidentes`
- [ ] El API Key está configurado correctamente (`X-API-Key: dev-key-12345`)
- [ ] El workflow tiene las URLs correctas (ya corregidas en `bot-telegram-CORREGIDO.json`)

---

## Conclusión

✅ **El workflow `bot-telegram-CORREGIDO.json` YA tiene las URLs corregidas** y se conectará correctamente con el microservicio.

**URLs correctas**:
- POST `http://microservicio:8080/api/incidentes`
- GET `http://microservicio:8080/api/incidentes/{id}/detalle`

**Próximo paso**: Importar el workflow en n8n y probarlo.
