# 🚀 Deploy en DigitalOcean con Docker Compose

Guía completa para desplegar el sistema de recepción de incidentes en DigitalOcean usando un Droplet y Docker Compose.

## 📋 Requisitos Previos

- ✅ Cuenta de DigitalOcean con crédito estudiantil ($200)
- ✅ Dominio propio (opcional, puedes usar la IP del Droplet inicialmente)
- ✅ Código del proyecto en GitHub
- ✅ Acceso SSH desde tu computadora

---

## 💰 Costos Estimados

Con tu crédito estudiantil de $200:

- **Droplet**: $12/mes (2GB RAM, 1 vCPU, 50GB SSD) - **RECOMENDADO**
- **Total**: $12/mes = ~16 meses de uso con tu crédito

---

## 🎯 Paso 1: Crear Droplet en DigitalOcean

### 1.1 Acceder a DigitalOcean

1. Ve a https://cloud.digitalocean.com
2. Inicia sesión con tu cuenta estudiantil
3. Click en **"Create"** → **"Droplets"**

### 1.2 Configurar el Droplet

**Choose an image:**
- Distributions: **Ubuntu 22.04 (LTS) x64**

**Choose Size:**
- Droplet Type: **Basic**
- CPU Options: **Regular**
- Plan: **$12/mo** (2 GB RAM / 1 vCPU / 50 GB SSD / 2 TB transfer)

**Choose a datacenter region:**
- New York (más cercano a México) o cualquier otra región

**Authentication:**
- ✅ **SSH Key** (RECOMENDADO - más seguro)
  - Click en "New SSH Key"
  - En tu computadora local ejecuta:
    ```bash
    # Generar SSH key si no tienes
    ssh-keygen -t rsa -b 4096 -C "tu-email@ejemplo.com"

    # Ver tu clave pública (copiar este contenido)
    cat ~/.ssh/id_rsa.pub
    ```
  - Pega el contenido en DigitalOcean
  - Dale un nombre: "Mi PC Principal"

O bien:

- ⚠️ **Password** (más simple pero menos seguro)

**Finalize Details:**
- Quantity: **1 Droplet**
- Hostname: **ms-recepcion** (o el que prefieras)

**Add tags:** (opcional)
- `produccion`, `docker`, `ms-recepcion`

### 1.3 Crear el Droplet

1. Click en **"Create Droplet"**
2. Espera 1-2 minutos a que se cree
3. **Guarda la IP del Droplet** (ejemplo: `159.89.123.45`)

---

## 🔧 Paso 2: Conectarse al Servidor

### 2.1 Conectar por SSH

En tu computadora local (Git Bash en Windows o Terminal en Linux/Mac):

```bash
# Reemplaza YOUR_DROPLET_IP con la IP de tu Droplet
ssh root@YOUR_DROPLET_IP
```

Si usaste password, ingresa la contraseña que te enviaron por email.

**¡Listo!** Ahora estás dentro del servidor.

---

## 🐳 Paso 3: Instalar Docker

### 3.1 Descargar script de instalación

```bash
# Descargar el script desde tu repositorio
curl -o install-server.sh https://raw.githubusercontent.com/Angela95J/Ms_recepcion/main/deploy/install-server.sh

# Dar permisos de ejecución
chmod +x install-server.sh

# Ejecutar instalación
./install-server.sh
```

### 3.2 Cerrar sesión y volver a conectar

```bash
# Cerrar sesión
exit

# Volver a conectar
ssh root@YOUR_DROPLET_IP
```

### 3.3 Verificar instalación

```bash
docker --version
docker compose version
docker ps
```

Si todo funciona sin errores, ¡Docker está instalado! ✅

---

## ⚙️ Paso 4: Configurar Variables de Entorno

### 4.1 Clonar el repositorio

```bash
# Ir al directorio home
cd ~

# Clonar tu proyecto
git clone https://github.com/Angela95J/Ms_recepcion.git ms-recepcion

# Entrar al directorio
cd ms-recepcion
```

### 4.2 Crear archivo .env.prod

```bash
# Copiar el ejemplo
cp .env.prod.example .env.prod

# Editar el archivo
nano .env.prod
```

### 4.3 Configurar valores (IMPORTANTE)

Edita estos valores en `.env.prod`:

```bash
# Base de datos - CAMBIAR CONTRASEÑA
POSTGRES_PASSWORD=tu_contraseña_segura_aqui_123ABC

# API Keys - GENERAR CLAVES SEGURAS
# Genera claves aleatorias con: openssl rand -hex 32
API_KEY_ADMIN=clave_admin_super_segura_generada_aleatoriamente
API_KEY_N8N=clave_n8n_super_segura_generada_aleatoriamente

# Dominio (usar IP si no tienes dominio)
DOMAIN=159.89.123.45  # Reemplaza con tu IP
APP_URL=http://159.89.123.45
WEBHOOK_URL=http://159.89.123.45
N8N_HOST=159.89.123.45

# Email para SSL (si usarás dominio más adelante)
SSL_EMAIL=tu-email@ejemplo.com

# Timezone
TZ=America/Mexico_City
```

**Guardar y salir:**
- Presiona `Ctrl + O` (guardar)
- Presiona `Enter` (confirmar)
- Presiona `Ctrl + X` (salir)

---

## 🚀 Paso 5: Desplegar la Aplicación

### 5.1 Ejecutar deploy

```bash
# Dar permisos al script
chmod +x deploy/deploy.sh

# Ejecutar deploy
./deploy/deploy.sh
```

Este script:
1. ✅ Actualiza el código desde GitHub
2. ✅ Construye las imágenes Docker
3. ✅ Inicia todos los servicios
4. ✅ Muestra el estado

### 5.2 Verificar que todo está corriendo

```bash
# Ver estado de contenedores
docker compose -f docker-compose.prod.yml ps

# Debería mostrar:
# ✅ recepcion-postgres      (healthy)
# ✅ recepcion-ml-texto      (running)
# ✅ recepcion-ml-imagen     (running)
# ✅ recepcion-microservicio (running)
# ✅ recepcion-n8n           (running)
# ✅ recepcion-nginx         (running)
```

### 5.3 Ver logs

```bash
# Logs de todos los servicios
docker compose -f docker-compose.prod.yml --env-file .env.prod logs -f

# Logs de un servicio específico
docker compose -f docker-compose.prod.yml logs -f microservicio
docker compose -f docker-compose.prod.yml logs -f n8n
docker compose -f docker-compose.prod.yml logs -f postgres

# Salir de los logs: Ctrl + C
```

---

## 🧪 Paso 6: Probar la Aplicación

### 6.1 Desde tu navegador

Abre tu navegador y prueba:

```
# API Principal
http://YOUR_DROPLET_IP/api/incidentes

# n8n (interfaz web)
http://YOUR_DROPLET_IP/n8n

# Health check
http://YOUR_DROPLET_IP/health
```

### 6.2 Desde Postman o cURL

```bash
# Crear un incidente de prueba
curl -X POST http://YOUR_DROPLET_IP/api/incidentes \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: tu_clave_n8n_aqui" \
  -d '{
    "titulo": "Prueba de producción",
    "descripcion": "Incidente de prueba desde DigitalOcean",
    "ubicacion": "Servidor en la nube",
    "prioridad": "MEDIA"
  }'

# Listar incidentes
curl http://YOUR_DROPLET_IP/api/incidentes
```

---

## 🔐 Paso 7: Configurar SSL/HTTPS (Opcional - Requiere Dominio)

Si tienes un dominio propio (ejemplo: `mi-app.com`):

### 7.1 Apuntar el dominio al Droplet

En tu proveedor de dominio (GoDaddy, Namecheap, etc.):

1. Crear un **registro A**:
   - Host: `@`
   - Value: IP de tu Droplet
   - TTL: 3600

2. Crear un **registro A** para www:
   - Host: `www`
   - Value: IP de tu Droplet
   - TTL: 3600

Espera 5-30 minutos para que se propague.

### 7.2 Actualizar .env.prod

```bash
nano .env.prod
```

Cambiar:
```bash
DOMAIN=mi-app.com
APP_URL=https://mi-app.com
WEBHOOK_URL=https://mi-app.com
N8N_HOST=mi-app.com
SSL_EMAIL=tu-email@ejemplo.com
```

### 7.3 Configurar Nginx

```bash
# Editar configuración de Nginx
nano nginx/conf.d/app.conf

# Reemplazar todas las ocurrencias de "tu-dominio.com" con tu dominio real
# Buscar: tu-dominio.com
# Reemplazar: mi-app.com
```

### 7.4 Obtener certificado SSL

```bash
# Dar permisos al script
chmod +x deploy/setup-ssl.sh

# Ejecutar
./deploy/setup-ssl.sh
```

### 7.5 Reiniciar Nginx

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod restart nginx
```

¡Ahora tu aplicación está en HTTPS! 🔐

---

## 🔄 Actualizar la Aplicación

Cuando hagas cambios en el código:

```bash
# Conectar al servidor
ssh root@YOUR_DROPLET_IP

# Ir al directorio
cd ~/ms-recepcion

# Ejecutar deploy (actualiza y reinicia)
./deploy/deploy.sh
```

---

## 📊 Monitoreo y Mantenimiento

### Ver uso de recursos

```bash
# CPU y RAM
docker stats

# Espacio en disco
df -h

# Logs del sistema
journalctl -xe
```

### Limpiar recursos

```bash
# Eliminar imágenes no usadas
docker image prune -a

# Eliminar volúmenes no usados
docker volume prune

# Liberar espacio
docker system prune -a
```

### Backups de base de datos

```bash
# Crear backup
docker exec recepcion-postgres pg_dump -U postgres MSrecepcion > backup_$(date +%Y%m%d).sql

# Restaurar backup
docker exec -i recepcion-postgres psql -U postgres MSrecepcion < backup_20250111.sql
```

---

## 🆘 Troubleshooting

### Los servicios no inician

```bash
# Ver logs detallados
docker compose -f docker-compose.prod.yml --env-file .env.prod logs

# Revisar estado
docker compose -f docker-compose.prod.yml ps -a

# Reiniciar todo
docker compose -f docker-compose.prod.yml --env-file .env.prod restart
```

### No puedo acceder desde el navegador

```bash
# Verificar firewall
sudo ufw status

# Verificar que nginx está corriendo
docker ps | grep nginx

# Ver logs de nginx
docker logs recepcion-nginx
```

### Servicios ML no responden

```bash
# Ver logs de ML
docker logs recepcion-ml-texto
docker logs recepcion-ml-imagen

# Reiniciar servicios ML
docker compose -f docker-compose.prod.yml restart ml-texto ml-imagen
```

### Falta de memoria

```bash
# Ver uso de memoria
free -h
docker stats --no-stream

# Si es necesario, aumenta el tamaño del Droplet:
# DigitalOcean → Droplets → Resize → $18/mo (4GB RAM)
```

---

## 🎓 Optimización de Costos (Crédito Estudiantil)

Con tu crédito de $200:

```
Droplet $12/mes × 16 meses = $192 ✅

Opciones extras (si necesitas):
- Backup automático: +20% ($2.40/mes)
- Monitoreo: Gratis
- Alertas: Gratis
```

**Recomendación:** Usa el Droplet de $12/mes. Es suficiente para desarrollo y proyectos estudiantiles.

---

## 📞 Soporte

Si tienes problemas:

1. **Revisa los logs:** `docker compose logs -f`
2. **Verifica el estado:** `docker compose ps`
3. **Consulta la documentación:** Este archivo
4. **Contacta al equipo:** GitHub Issues

---

## ✅ Checklist Final

- [ ] Droplet creado y accesible por SSH
- [ ] Docker instalado y funcionando
- [ ] Código clonado desde GitHub
- [ ] Archivo `.env.prod` configurado
- [ ] Deploy ejecutado exitosamente
- [ ] Todos los contenedores corriendo
- [ ] API respondiendo en `/api/incidentes`
- [ ] n8n accesible en `/n8n`
- [ ] (Opcional) SSL configurado con dominio

---

¡Felicidades! Tu aplicación está corriendo en producción en DigitalOcean. 🎉
