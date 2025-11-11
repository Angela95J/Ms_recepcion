#!/bin/bash
# ====================================
# SCRIPT DE DEPLOY - PRODUCCIÓN
# ====================================

set -e

PROJECT_DIR="$HOME/ms-recepcion"
REPO_URL="https://github.com/Angela95J/Ms_recepcion.git"
BRANCH="main"

echo "======================================"
echo "🚀 Desplegando MS Recepción"
echo "======================================"

# Crear directorio si no existe
if [ ! -d "$PROJECT_DIR" ]; then
    echo "→ Clonando repositorio..."
    git clone $REPO_URL $PROJECT_DIR
    cd $PROJECT_DIR
    git checkout $BRANCH
else
    echo "→ Actualizando código..."
    cd $PROJECT_DIR
    git fetch origin
    git checkout $BRANCH
    git pull origin $BRANCH
fi

# Verificar que existe .env.prod
if [ ! -f ".env.prod" ]; then
    echo "❌ ERROR: No existe el archivo .env.prod"
    echo "   Copia .env.prod.example a .env.prod y configura las variables"
    echo "   cp .env.prod.example .env.prod"
    echo "   nano .env.prod"
    exit 1
fi

# Crear directorios necesarios
echo "→ Creando directorios necesarios..."
mkdir -p certbot/conf certbot/www

# Detener contenedores existentes
echo "→ Deteniendo contenedores existentes..."
docker compose -f docker-compose.prod.yml --env-file .env.prod down || true

# Construir imágenes
echo "→ Construyendo imágenes Docker..."
docker compose -f docker-compose.prod.yml --env-file .env.prod build --no-cache

# Iniciar servicios
echo "→ Iniciando servicios..."
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d

# Esperar a que los servicios estén listos
echo "→ Esperando a que los servicios inicien..."
sleep 20

# Mostrar estado
echo ""
echo "→ Estado de los contenedores:"
docker compose -f docker-compose.prod.yml ps

echo ""
echo "======================================"
echo "✅ Deploy completado!"
echo "======================================"
echo ""
echo "Logs en tiempo real:"
echo "  docker compose -f docker-compose.prod.yml --env-file .env.prod logs -f"
echo ""
echo "Ver logs de un servicio específico:"
echo "  docker compose -f docker-compose.prod.yml logs -f microservicio"
echo ""
echo "Detener todo:"
echo "  docker compose -f docker-compose.prod.yml --env-file .env.prod down"
