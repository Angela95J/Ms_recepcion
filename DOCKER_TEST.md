# 🧪 Guía de Pruebas - Dockerización

## 📋 Checklist de Verificación

Sigue estos pasos para verificar que la dockerización funciona correctamente.

---

## 1️⃣ Pre-requisitos

### Verificar Docker
```bash
# Verificar instalación
docker --version
docker-compose --version

# Verificar que Docker está corriendo
docker info
```

**Resultado esperado:**
```
Docker version 24.0.0 o superior
Docker Compose version 2.0.0 o superior
```

---

## 2️⃣ Iniciar Servicios

### Windows
```bash
start-app.bat
```

### Linux/Mac
```bash
chmod +x *.sh
./start-app.sh
```

### Manual
```bash
docker-compose -f docker-compose.app.yml up -d --build
```

**Tiempo estimado:** 5-10 minutos (primera vez con build)

---

## 3️⃣ Verificar Estado de Servicios

```bash
docker-compose -f docker-compose.app.yml ps
```

**Resultado esperado:**
```
NAME                        STATUS              PORTS
recepcion-postgres          Up (healthy)        0.0.0.0:5432->5432/tcp
recepcion-ml-texto          Up (healthy)        0.0.0.0:8001->8001/tcp
recepcion-ml-imagen         Up (healthy)        0.0.0.0:8002->8002/tcp
recepcion-microservicio     Up (healthy)        0.0.0.0:8080->8080/tcp
```

✅ **Todos los servicios deben estar en "Up (healthy)"**

Si alguno está "unhealthy", ver logs:
```bash
docker-compose -f docker-compose.app.yml logs [nombre-servicio]
```

---

## 4️⃣ Test de Conectividad

### Test 1: PostgreSQL
```bash
# Conectarse a PostgreSQL
docker exec -it recepcion-postgres psql -U postgres -d MSrecepcion

# Dentro de psql, ejecutar:
\dt     # Ver tablas
\q      # Salir
```

**Resultado esperado:**
```
List of relations
 Schema |            Name             | Type  |  Owner
--------+-----------------------------+-------+----------
 public | analisis_ml_imagen          | table | postgres
 public | analisis_ml_texto           | table | postgres
 public | incidente                   | table | postgres
 ...
```

### Test 2: ML Texto
```bash
# Health check
curl http://localhost:8001/api/ml/salud

# O abrir en navegador
# http://localhost:8001/docs
```

**Resultado esperado:**
```json
{
  "status": "ok",
  "servicio": "ML Análisis de Texto",
  "modelo_cargado": true
}
```

### Test 3: ML Imagen
```bash
# Health check
curl http://localhost:8002/api/ml/salud

# O abrir en navegador
# http://localhost:8002/docs
```

**Resultado esperado:**
```json
{
  "status": "ok",
  "servicio": "ML Análisis de Imágenes",
  "modelo_cargado": true
}
```

### Test 4: Microservicio
```bash
# Health check
curl http://localhost:8080/api/actuator/health

# O abrir Swagger UI
# http://localhost:8080/api/swagger-ui.html
```

**Resultado esperado:**
```json
{
  "status": "UP"
}
```

---

## 5️⃣ Test de Integración Completa

### Test A: Crear un incidente

```bash
curl -X POST "http://localhost:8080/api/incidentes" \
  -H "X-API-Key: dev-key-12345" \
  -H "Content-Type: application/json" \
  -d '{
    "solicitante": {
      "nombre": "Juan Pérez",
      "telefono": "+59170123456"
    },
    "ubicacion": {
      "latitud": -17.3935,
      "longitud": -66.1570,
      "direccion": "Av. Heroínas, Cochabamba"
    },
    "descripcion": "Accidente grave con heridos en Av. Heroínas. Necesito ambulancia urgente.",
    "tipo": "EMERGENCIA_MEDICA"
  }'
```

**Resultado esperado:**
```json
{
  "id": 1,
  "codigo": "INC-20250107-0001",
  "estado": "PENDIENTE",
  "prioridadFinal": null,
  ...
}
```

✅ **Debe retornar un incidente creado con código único**

### Test B: Verificar análisis ML automático

Espera 5-10 segundos para que se procese el análisis ML.

```bash
# Obtener el incidente creado (reemplaza {id} con el ID del test anterior)
curl -X GET "http://localhost:8080/api/incidentes/{id}" \
  -H "X-API-Key: dev-key-12345"
```

**Resultado esperado:**
```json
{
  "id": 1,
  "codigo": "INC-20250107-0001",
  "estado": "ANALIZADO",
  "prioridadFinal": 4,
  "analisisTexto": {
    "prioridad": 4,
    "scoreConfianza": 0.85,
    ...
  }
}
```

✅ **Debe tener `estado: "ANALIZADO"` y `prioridadFinal` calculada**

### Test C: Subir una imagen

```bash
# Preparar una imagen de prueba
# (Asume que tienes una imagen llamada test.jpg)

curl -X POST "http://localhost:8080/api/multimedia/{id}/imagenes" \
  -H "X-API-Key: dev-key-12345" \
  -F "file=@test.jpg"
```

**Resultado esperado:**
```json
{
  "id": 1,
  "tipo": "IMAGEN",
  "nombreArchivo": "test.jpg",
  "rutaArchivo": "/api/multimedia/archivos/...",
  "fechaSubida": "2025-01-07T..."
}
```

### Test D: Verificar análisis de imagen

Espera 5-10 segundos.

```bash
curl -X GET "http://localhost:8080/api/incidentes/{id}/analisis-imagen" \
  -H "X-API-Key: dev-key-12345"
```

**Resultado esperado:**
```json
{
  "severidadDetectada": 3,
  "scoreVeracidad": 0.78,
  "objetosDetectados": [...],
  ...
}
```

---

## 6️⃣ Test de Volúmenes Persistentes

### Test de persistencia de PostgreSQL

```bash
# 1. Crear un incidente (como en Test A)

# 2. Detener servicios
docker-compose -f docker-compose.app.yml down

# 3. Reiniciar servicios
docker-compose -f docker-compose.app.yml up -d

# 4. Verificar que el incidente sigue ahí
curl -X GET "http://localhost:8080/api/incidentes" \
  -H "X-API-Key: dev-key-12345"
```

✅ **Los datos deben persistir después de reiniciar**

### Test de persistencia de archivos

```bash
# 1. Subir una imagen (como en Test C)

# 2. Listar volúmenes
docker volume ls | grep recepcion

# 3. Inspeccionar volumen de uploads
docker volume inspect recepcion_uploads_data
```

✅ **Debe existir el volumen `recepcion_uploads_data`**

---

## 7️⃣ Test de Logs

```bash
# Ver logs de todos los servicios
docker-compose -f docker-compose.app.yml logs -f

# Ver logs de un servicio específico
docker-compose -f docker-compose.app.yml logs -f microservicio

# Ver últimas 50 líneas
docker-compose -f docker-compose.app.yml logs --tail=50
```

✅ **No debe haber errores críticos en los logs**

---

## 8️⃣ Test de Performance

### Tiempo de inicio (cold start)
```bash
time docker-compose -f docker-compose.app.yml up -d --build
```

**Tiempo esperado:**
- Primera vez (con build): 5-10 minutos
- Subsecuentes (sin build): 30-60 segundos

### Tiempo de respuesta de APIs
```bash
# Medir tiempo de respuesta
time curl http://localhost:8080/api/swagger-ui.html > /dev/null
```

**Tiempo esperado:** < 2 segundos

---

## 9️⃣ Test de Recursos

```bash
# Ver uso de recursos
docker stats

# Ver uso de disco
docker system df
```

**Resultado esperado:**
```
CONTAINER           CPU %   MEM USAGE / LIMIT     MEM %
recepcion-postgres  < 5%    100-200 MB            < 10%
recepcion-ml-texto  < 5%    200-300 MB            < 15%
recepcion-ml-imagen < 5%    300-400 MB            < 20%
recepcion-microserv < 10%   500-800 MB            < 30%
```

---

## 🔟 Test de Networking

### Verificar red Docker
```bash
docker network ls
docker network inspect recepcion-network
```

### Test de comunicación interna
```bash
# Desde el microservicio, hacer ping a postgres
docker exec recepcion-microservicio ping -c 3 postgres

# Desde el microservicio, hacer request a ML texto
docker exec recepcion-microservicio wget -O- http://ml-texto:8001/api/ml/salud
```

✅ **Los servicios deben poder comunicarse entre sí**

---

## 🛑 Limpieza

Después de las pruebas:

```bash
# Detener servicios
docker-compose -f docker-compose.app.yml down

# Detener y eliminar volúmenes (⚠️ BORRA DATOS)
docker-compose -f docker-compose.app.yml down -v

# Eliminar imágenes creadas
docker image prune -a
```

---

## ✅ Checklist Final

- [ ] ✅ Docker y Docker Compose instalados
- [ ] ✅ Servicios iniciados correctamente
- [ ] ✅ Todos los health checks en "healthy"
- [ ] ✅ PostgreSQL accesible y con tablas creadas
- [ ] ✅ ML Texto responde correctamente
- [ ] ✅ ML Imagen responde correctamente
- [ ] ✅ Microservicio responde correctamente
- [ ] ✅ Crear incidente funciona
- [ ] ✅ Análisis ML automático se ejecuta
- [ ] ✅ Subir imagen funciona
- [ ] ✅ Análisis de imagen se ejecuta
- [ ] ✅ Datos persisten después de reiniciar
- [ ] ✅ Logs no tienen errores críticos
- [ ] ✅ Performance aceptable
- [ ] ✅ Uso de recursos razonable
- [ ] ✅ Comunicación entre servicios funciona

---

## 🐛 Problemas Comunes

Ver: [DOCKER_GUIDE.md - Troubleshooting](DOCKER_GUIDE.md#troubleshooting)

---

**Última actualización:** 2025-01-07
