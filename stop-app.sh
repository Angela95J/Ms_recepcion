#!/bin/bash
# Script para detener todos los servicios de la aplicación en Linux/Mac

echo "================================================"
echo " Deteniendo Microservicio de Recepcion"
echo "================================================"
echo ""

docker-compose -f docker-compose.app.yml down

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Hubo un problema al detener los servicios"
    exit 1
fi

echo ""
echo "[INFO] Servicios detenidos correctamente"
echo ""
