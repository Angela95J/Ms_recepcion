#!/bin/bash
# Script para iniciar n8n en Linux/Mac

echo "================================================"
echo " Iniciando n8n - Bot de Solicitud de Ambulancias"
echo "================================================"
echo ""

# Verificar que Docker está corriendo
if ! docker info > /dev/null 2>&1; then
    echo "[ERROR] Docker no esta corriendo. Por favor inicia Docker."
    exit 1
fi

echo "[INFO] Docker esta corriendo correctamente"
echo ""

# Verificar que existe el archivo .env.n8n
if [ ! -f .env.n8n ]; then
    echo "[WARNING] No existe el archivo .env.n8n"
    echo "[INFO] Creando .env.n8n desde .env.n8n.example..."
    cp .env.n8n.example .env.n8n
    echo "[INFO] Por favor revisa y ajusta el archivo .env.n8n si es necesario"
    echo ""
fi

# Verificar que la red compartida existe
if ! docker network ls | grep -q recepcion-network; then
    echo "[WARNING] La red recepcion-network no existe"
    echo "[INFO] Creando la red compartida..."
    docker network create recepcion-network
    echo ""
fi

echo "[INFO] Iniciando n8n..."
echo ""

docker-compose --env-file .env.n8n -f docker-compose.n8n.yml up -d

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Hubo un problema al iniciar n8n"
    exit 1
fi

echo ""
echo "================================================"
echo " n8n iniciado correctamente!"
echo "================================================"
echo ""
echo "Accede a n8n en:"
echo " URL:       http://localhost:5678"
echo " Usuario:   admin"
echo " Password:  admin123"
echo ""
echo "IMPORTANTE: Cambia las credenciales en produccion!"
echo ""
echo "Para ver los logs: docker-compose -f docker-compose.n8n.yml logs -f"
echo "Para detener:      docker-compose -f docker-compose.n8n.yml down"
echo ""
echo "Proximos pasos:"
echo " 1. Configura credenciales de WhatsApp/Telegram en n8n"
echo " 2. Crea los workflows de bots"
echo " 3. Configura webhooks en WhatsApp/Telegram"
echo ""
echo "Ver documentacion: n8n/README.md"
echo ""
