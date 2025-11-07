#!/bin/bash
# Script para detener n8n en Linux/Mac

echo "================================================"
echo " Deteniendo n8n"
echo "================================================"
echo ""

docker-compose -f docker-compose.n8n.yml down

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Hubo un problema al detener n8n"
    exit 1
fi

echo ""
echo "[INFO] n8n detenido correctamente"
echo ""
