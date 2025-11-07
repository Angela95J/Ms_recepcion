#!/bin/bash
# Script para iniciar todos los servicios de la aplicación en Linux/Mac

echo "================================================"
echo " Iniciando Microservicio de Recepcion"
echo " PostgreSQL + Microservicio + ML Texto + ML Imagen"
echo "================================================"
echo ""

# Verificar que Docker está corriendo
if ! docker info > /dev/null 2>&1; then
    echo "[ERROR] Docker no esta corriendo. Por favor inicia Docker."
    exit 1
fi

echo "[INFO] Docker esta corriendo correctamente"
echo ""

# Verificar que existe el archivo .env
if [ ! -f .env ]; then
    echo "[WARNING] No existe el archivo .env"
    echo "[INFO] Creando .env desde .env.example..."
    cp .env.example .env
    echo "[INFO] Por favor revisa y ajusta el archivo .env si es necesario"
    echo ""
fi

echo "[INFO] Construyendo e iniciando servicios..."
echo ""

docker-compose -f docker-compose.app.yml up -d --build

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Hubo un problema al iniciar los servicios"
    exit 1
fi

echo ""
echo "================================================"
echo " Servicios iniciados correctamente!"
echo "================================================"
echo ""
echo "Servicios disponibles:"
echo " - PostgreSQL:      http://localhost:5432"
echo " - Microservicio:   http://localhost:8080/api"
echo " - ML Texto:        http://localhost:8001"
echo " - ML Imagen:       http://localhost:8002"
echo ""
echo "Documentacion API:"
echo " - Swagger UI:      http://localhost:8080/api/swagger-ui.html"
echo " - ML Texto Docs:   http://localhost:8001/docs"
echo " - ML Imagen Docs:  http://localhost:8002/docs"
echo ""
echo "Para ver los logs: docker-compose -f docker-compose.app.yml logs -f"
echo "Para detener:      docker-compose -f docker-compose.app.yml down"
echo ""
