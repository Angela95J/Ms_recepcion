#!/bin/bash
# Script para detener TODOS los servicios (App + n8n) en Linux/Mac

echo "================================================"
echo " Deteniendo Sistema Completo"
echo "================================================"
echo ""

echo "[INFO] Deteniendo n8n..."
docker-compose -f docker-compose.n8n.yml down

echo ""
echo "[INFO] Deteniendo stack principal..."
docker-compose -f docker-compose.app.yml down

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Hubo un problema al detener los servicios"
    exit 1
fi

echo ""
echo "================================================"
echo " Todos los servicios detenidos correctamente"
echo "================================================"
echo ""
echo "Para iniciar nuevamente: ./start-all.sh"
echo ""
