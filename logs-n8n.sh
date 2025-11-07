#!/bin/bash
# Script para ver los logs de n8n en Linux/Mac

echo "================================================"
echo " Logs de n8n"
echo " Presiona Ctrl+C para salir"
echo "================================================"
echo ""

docker-compose -f docker-compose.n8n.yml logs -f
