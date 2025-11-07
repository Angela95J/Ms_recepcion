# 🐳 Docker - Inicio Rápido

## ⚡ Inicio Rápido (5 minutos)

### Windows
```bash
# 1. Iniciar Docker Desktop

# 2. Ejecutar el script de inicio
start-app.bat
```

### Linux/Mac
```bash
# 1. Dar permisos (solo primera vez)
chmod +x *.sh

# 2. Ejecutar el script de inicio
./start-app.sh
```

### Verificar que funciona

Abre tu navegador en:
- **API Principal:** http://localhost:8080/api/swagger-ui.html
- **ML Texto:** http://localhost:8001/docs
- **ML Imagen:** http://localhost:8002/docs

---

## 📋 Comandos Esenciales

```bash
# Ver logs en tiempo real
docker-compose -f docker-compose.app.yml logs -f

# Detener todo
docker-compose -f docker-compose.app.yml down

# Reiniciar un servicio
docker-compose -f docker-compose.app.yml restart microservicio
```

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────┐
│          Docker Compose (Stack)             │
├─────────────────────────────────────────────┤
│                                             │
│  ┌──────────────┐      ┌─────────────────┐ │
│  │  PostgreSQL  │◄─────┤ Microservicio   │ │
│  │  (Puerto     │      │ Spring Boot     │ │
│  │   5432)      │      │ (Puerto 8080)   │ │
│  └──────────────┘      └────────┬────────┘ │
│                                 │          │
│                        ┌────────┴────────┐ │
│                        │                 │ │
│              ┌─────────▼──────┐ ┌───────▼────────┐
│              │   ML Texto     │ │  ML Imagen     │
│              │   FastAPI      │ │  FastAPI       │
│              │  (Puerto 8001) │ │ (Puerto 8002)  │
│              └────────────────┘ └────────────────┘
│                                             │
└─────────────────────────────────────────────┘
         Red: recepcion-network
```

---

## 🔧 Configuración

Edita el archivo `.env` para cambiar puertos, contraseñas, etc:

```env
# PostgreSQL
POSTGRES_DB=MSrecepcion
POSTGRES_USER=postgres
POSTGRES_PASSWORD=123456

# Microservicio
MICROSERVICIO_PORT=8080

# Servicios ML
ML_TEXTO_PORT=8001
ML_IMAGEN_PORT=8002
```

---

## 🐛 Problemas Comunes

### Los servicios no inician
```bash
# Ver logs
docker-compose -f docker-compose.app.yml logs

# Verificar que los puertos no están en uso
netstat -ano | findstr :8080    # Windows
lsof -i :8080                   # Linux/Mac
```

### Cambios en código no se reflejan
```bash
# Rebuild forzado
docker-compose -f docker-compose.app.yml build --no-cache
docker-compose -f docker-compose.app.yml up -d
```

---

## 📚 Documentación Completa

Para más detalles, consulta: **[DOCKER_GUIDE.md](DOCKER_GUIDE.md)**

---

## 🚀 Próximos Pasos

1. ✅ Dockerización completada
2. 🔄 Siguiente: Integrar n8n para el bot de WhatsApp/Telegram
3. 📦 Crear `docker-compose.n8n.yml` separado

---

**¿Listo para producción?** Revisa la sección de [Producción en DOCKER_GUIDE.md](DOCKER_GUIDE.md#producción)
