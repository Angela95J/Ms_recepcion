# 🐳 Guía de Dockerización - Microservicio de Recepción de Incidentes

## 📋 Tabla de Contenidos
- [Introducción](#introducción)
- [Requisitos Previos](#requisitos-previos)
- [Arquitectura Docker](#arquitectura-docker)
- [Inicio Rápido](#inicio-rápido)
- [Configuración](#configuración)
- [Comandos Útiles](#comandos-útiles)
- [Troubleshooting](#troubleshooting)
- [Producción](#producción)

---

## 🎯 Introducción

Esta guía describe cómo ejecutar el microservicio de recepción de incidentes completo usando Docker y Docker Compose. Todos los servicios están contenedorizados:

- **PostgreSQL** - Base de datos
- **Microservicio Spring Boot** - API REST principal
- **ML Texto** - Servicio de análisis de texto con FastAPI
- **ML Imagen** - Servicio de análisis de imágenes con FastAPI

---

## 📦 Requisitos Previos

### Software Necesario

1. **Docker Desktop** (Windows/Mac) o **Docker Engine** (Linux)
   - Windows: https://docs.docker.com/desktop/install/windows-install/
   - Mac: https://docs.docker.com/desktop/install/mac-install/
   - Linux: https://docs.docker.com/engine/install/

2. **Docker Compose** (incluido en Docker Desktop)
   - Versión mínima: 2.0+

### Verificar Instalación

```bash
# Verificar Docker
docker --version
# Salida esperada: Docker version 24.0+ o superior

# Verificar Docker Compose
docker-compose --version
# Salida esperada: Docker Compose version 2.0+ o superior

# Verificar que Docker está corriendo
docker info
```

### Recursos Mínimos Recomendados

- **RAM**: 4 GB mínimo, 8 GB recomendado
- **Disco**: 10 GB de espacio libre
- **CPU**: 2 cores mínimo, 4 cores recomendado

---

## 🏗️ Arquitectura Docker

### Servicios y Puertos

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| **postgres** | 5432 | Base de datos PostgreSQL 15 |
| **microservicio** | 8080 | API REST Spring Boot |
| **ml-texto** | 8001 | Análisis ML de texto (FastAPI) |
| **ml-imagen** | 8002 | Análisis ML de imágenes (FastAPI) |

### Red Docker

Todos los servicios están conectados a la red `recepcion-network` (bridge).

**Comunicación interna:**
- Microservicio → PostgreSQL: `postgres:5432`
- Microservicio → ML Texto: `http://ml-texto:8001`
- Microservicio → ML Imagen: `http://ml-imagen:8002`

### Volúmenes Persistentes

```yaml
postgres_data:      # Datos de PostgreSQL
uploads_data:       # Archivos multimedia subidos
```

---

## 🚀 Inicio Rápido

### Opción 1: Scripts Automatizados (Recomendado)

#### Windows
```bash
# Iniciar todos los servicios
start-app.bat

# Ver logs en tiempo real
logs-app.bat

# Detener todos los servicios
stop-app.bat
```

#### Linux/Mac
```bash
# Dar permisos de ejecución (solo primera vez)
chmod +x *.sh

# Iniciar todos los servicios
./start-app.sh

# Ver logs en tiempo real
./logs-app.sh

# Detener todos los servicios
./stop-app.sh
```

### Opción 2: Comandos Docker Compose Manuales

```bash
# 1. Construir e iniciar servicios
docker-compose -f docker-compose.app.yml up -d --build

# 2. Ver logs
docker-compose -f docker-compose.app.yml logs -f

# 3. Detener servicios
docker-compose -f docker-compose.app.yml down
```

### Verificar que todo está funcionando

Después de iniciar, verifica los servicios:

```bash
# Ver estado de los contenedores
docker-compose -f docker-compose.app.yml ps build

# Deberías ver 4 servicios en estado "Up" y "healthy"
```

**Endpoints de verificación:**
- Microservicio: http://localhost:8080/api/swagger-ui.html
- ML Texto: http://localhost:8001/docs
- ML Imagen: http://localhost:8002/docs

---

## ⚙️ Configuración

### Archivo .env

El archivo `.env` contiene todas las variables de entorno. Se crea automáticamente desde `.env.example`.

```bash
# Ubicación
ms_recepcion/.env
```

**Variables principales:**

```env
# PostgreSQL
POSTGRES_DB=MSrecepcion
POSTGRES_USER=postgres
POSTGRES_PASSWORD=123456
POSTGRES_PORT=5432

# Microservicio
MICROSERVICIO_PORT=8080
SPRING_PROFILE=prod
JAVA_OPTS=-Xms512m -Xmx1024m

# API Keys (cambiar en producción!)
API_KEYS=dev-key-12345,admin-key-67890

# Servicios ML
ML_TEXTO_PORT=8001
ML_IMAGEN_PORT=8002
```

### Personalizar Configuración

1. **Editar el archivo .env**
   ```bash
   notepad .env          # Windows
   nano .env             # Linux/Mac
   ```

2. **Reiniciar servicios para aplicar cambios**
   ```bash
   docker-compose -f docker-compose.app.yml down
   docker-compose -f docker-compose.app.yml up -d
   ```

---

## 🛠️ Comandos Útiles

### Gestión de Servicios

```bash
# Iniciar servicios (sin rebuild)
docker-compose -f docker-compose.app.yml up -d

# Iniciar con rebuild (después de cambios en código)
docker-compose -f docker-compose.app.yml up -d --build

# Detener servicios (mantiene volúmenes)
docker-compose -f docker-compose.app.yml down

# Detener y eliminar volúmenes (⚠️ borra datos!)
docker-compose -f docker-compose.app.yml down -v

# Reiniciar un servicio específico
docker-compose -f docker-compose.app.yml restart microservicio
```

### Logs y Debugging

```bash
# Ver logs de todos los servicios
docker-compose -f docker-compose.app.yml logs -f

# Ver logs de un servicio específico
docker-compose -f docker-compose.app.yml logs -f microservicio
docker-compose -f docker-compose.app.yml logs -f postgres
docker-compose -f docker-compose.app.yml logs -f ml-texto
docker-compose -f docker-compose.app.yml logs -f ml-imagen

# Ver últimas 100 líneas de logs
docker-compose -f docker-compose.app.yml logs --tail=100

# Ejecutar comando dentro de un contenedor
docker exec -it recepcion-microservicio bash
docker exec -it recepcion-postgres psql -U postgres -d MSrecepcion
docker exec -it recepcion-ml-texto bash
```

### Estado y Monitoreo

```bash
# Ver estado de servicios
docker-compose -f docker-compose.app.yml ps

# Ver uso de recursos
docker stats

# Ver redes
docker network ls

# Ver volúmenes
docker volume ls

# Inspeccionar un servicio
docker inspect recepcion-microservicio
```

### Limpieza

```bash
# Eliminar imágenes no utilizadas
docker image prune -a

# Eliminar volúmenes no utilizados
docker volume prune

# Limpieza completa del sistema Docker (⚠️ cuidado!)
docker system prune -a --volumes
```

---

## 🐛 Troubleshooting

### Problema: Los servicios no inician

**Síntoma:** `docker-compose up` falla o los contenedores se cierran inmediatamente.

**Soluciones:**

1. **Verificar logs**
   ```bash
   docker-compose -f docker-compose.app.yml logs
   ```

2. **Verificar que los puertos no están en uso**
   ```bash
   # Windows
   netstat -ano | findstr :8080
   netstat -ano | findstr :5432

   # Linux/Mac
   lsof -i :8080
   lsof -i :5432
   ```

3. **Verificar recursos de Docker**
   - Docker Desktop → Settings → Resources
   - Asignar al menos 4 GB de RAM

### Problema: PostgreSQL no está listo

**Síntoma:** Microservicio falla con `connection refused` a PostgreSQL.

**Solución:**

1. **Verificar health check de PostgreSQL**
   ```bash
   docker-compose -f docker-compose.app.yml ps
   # Debe mostrar "healthy" en postgres
   ```

2. **Esperar más tiempo** - El microservicio tiene `depends_on` con condición `service_healthy`

3. **Verificar logs de PostgreSQL**
   ```bash
   docker-compose -f docker-compose.app.yml logs postgres
   ```

### Problema: Servicios ML no responden

**Síntoma:** Microservicio no puede conectarse a ML Texto o ML Imagen.

**Soluciones:**

1. **Verificar que los modelos ML están entrenados**
   ```bash
   # Verificar que existen los modelos
   ls ml_analisis_texto/trained_models/
   ls ml_analisis_imagen/trained_models/
   ```

2. **Ver logs de servicios ML**
   ```bash
   docker-compose -f docker-compose.app.yml logs ml-texto
   docker-compose -f docker-compose.app.yml logs ml-imagen
   ```

3. **Verificar health checks**
   ```bash
   curl http://localhost:8001/api/ml/salud
   curl http://localhost:8002/api/ml/salud
   ```

### Problema: Error de permisos en volúmenes

**Síntoma:** Contenedores no pueden escribir en volúmenes.

**Solución (Linux/Mac):**
```bash
# Dar permisos a directorios compartidos
sudo chown -R $USER:$USER ml_analisis_texto/trained_models
sudo chown -R $USER:$USER ml_analisis_imagen/trained_models
```

### Problema: Build falla por falta de memoria

**Síntoma:** Maven o pip fallan durante el build.

**Solución:**

1. **Aumentar memoria de Docker**
   - Docker Desktop → Settings → Resources → Memory: 6-8 GB

2. **Build un servicio a la vez**
   ```bash
   docker-compose -f docker-compose.app.yml build postgres
   docker-compose -f docker-compose.app.yml build ml-texto
   docker-compose -f docker-compose.app.yml build ml-imagen
   docker-compose -f docker-compose.app.yml build microservicio
   ```

### Problema: Cambios en código no se reflejan

**Síntoma:** Modificaste el código pero los cambios no aparecen.

**Solución:**
```bash
# Rebuild forzado
docker-compose -f docker-compose.app.yml build --no-cache microservicio
docker-compose -f docker-compose.app.yml up -d microservicio
```

---

## 🚀 Producción

### Consideraciones de Seguridad

1. **Cambiar API Keys**
   ```env
   # .env
   API_KEYS=secure-key-prod-xxxxx,admin-key-prod-yyyyy
   ```

2. **Cambiar contraseña de PostgreSQL**
   ```env
   POSTGRES_PASSWORD=una_contraseña_muy_segura_y_larga
   ```

3. **Usar HTTPS** - Configurar un reverse proxy (nginx, traefik)

4. **No exponer puertos innecesarios**
   - Solo exponer 8080 (microservicio) al exterior
   - PostgreSQL, ML servicios solo en red interna

### Variables de Entorno para Producción

```env
# .env (producción)
SPRING_PROFILE=prod
JAVA_OPTS=-Xms1024m -Xmx2048m
POSTGRES_PASSWORD=secure_password_here
API_KEYS=prod-key-1,prod-key-2
```

### Backup de Datos

```bash
# Backup de PostgreSQL
docker exec recepcion-postgres pg_dump -U postgres MSrecepcion > backup.sql

# Restaurar backup
docker exec -i recepcion-postgres psql -U postgres MSrecepcion < backup.sql

# Backup de volúmenes
docker run --rm -v recepcion_postgres_data:/data -v $(pwd):/backup \
  alpine tar czf /backup/postgres_backup.tar.gz /data
```

### Logging en Producción

Configurar logging driver en docker-compose:

```yaml
services:
  microservicio:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### Monitoreo

Considera agregar:
- **Prometheus** + **Grafana** - Métricas
- **ELK Stack** - Logs centralizados
- **Health checks** - Monitoreo de disponibilidad

---

## 📚 Recursos Adicionales

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker)
- [FastAPI Docker Guide](https://fastapi.tiangolo.com/deployment/docker/)

---

## 🤝 Soporte

Si encuentras problemas:

1. Revisa la sección [Troubleshooting](#troubleshooting)
2. Verifica los logs de los servicios
3. Consulta la documentación del proyecto

---

**Última actualización:** 2025-01-07
