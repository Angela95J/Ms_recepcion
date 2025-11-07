#!/bin/bash
# Script para ver los logs de todos los servicios en Linux/Mac

echo "================================================"
echo " Logs del Microservicio de Recepcion"
echo " Presiona Ctrl+C para salir"
echo "================================================"
echo ""

docker-compose -f docker-compose.app.yml logs -f
