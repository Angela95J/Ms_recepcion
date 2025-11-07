#!/bin/bash
# Script para iniciar TODOS los servicios (App + n8n) en Linux/Mac

echo "================================================"
echo " Iniciando Sistema Completo de Recepcion"
echo " Microservicio + ML + PostgreSQL + n8n"
echo "================================================"
echo ""

# Verificar que Docker está corriendo
if ! docker info > /dev/null 2>&1; then
    echo "[ERROR] Docker no esta corriendo. Por favor inicia Docker."
    exit 1
fi

echo "[INFO] Docker esta corriendo correctamente"
echo ""

# ====================================
# PASO 1: Iniciar Stack Principal
# ====================================
echo "================================================"
echo " PASO 1/2: Iniciando Stack Principal"
echo " (PostgreSQL + Microservicio + ML)"
echo "================================================"
echo ""

./start-app.sh
if [ $? -ne 0 ]; then
    echo "[ERROR] Fallo al iniciar el stack principal"
    exit 1
fi

echo ""
echo "[INFO] Stack principal iniciado correctamente"
echo ""

# Esperar 10 segundos para que los servicios estén listos
echo "[INFO] Esperando 10 segundos para que los servicios se estabilicen..."
sleep 10

# ====================================
# PASO 2: Iniciar n8n
# ====================================
echo "================================================"
echo " PASO 2/2: Iniciando n8n"
echo "================================================"
echo ""

./start-n8n.sh
if [ $? -ne 0 ]; then
    echo "[ERROR] Fallo al iniciar n8n"
    exit 1
fi

echo ""
echo "================================================"
echo " SISTEMA COMPLETO INICIADO!"
echo "================================================"
echo ""
echo "Servicios disponibles:"
echo ""
echo " BACKEND:"
echo "  - PostgreSQL:      localhost:5432"
echo "  - Microservicio:   http://localhost:8080/api"
echo "  - Swagger UI:      http://localhost:8080/api/swagger-ui.html"
echo "  - ML Texto:        http://localhost:8001"
echo "  - ML Imagen:       http://localhost:8002"
echo ""
echo " BOT / AUTOMATIZACION:"
echo "  - n8n:             http://localhost:5678"
echo "  - Usuario:         admin"
echo "  - Password:        admin123"
echo ""
echo "Comandos utiles:"
echo "  Ver logs app:      docker-compose -f docker-compose.app.yml logs -f"
echo "  Ver logs n8n:      docker-compose -f docker-compose.n8n.yml logs -f"
echo "  Detener todo:      ./stop-all.sh"
echo ""
echo "Proximos pasos:"
echo "  1. Abre n8n: http://localhost:5678"
echo "  2. Configura credenciales de WhatsApp/Telegram"
echo "  3. Crea workflows de bots (ver n8n/README.md)"
echo "  4. Prueba el flujo completo"
echo ""
