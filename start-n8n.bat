@echo off
REM Script para iniciar n8n en Windows

echo ================================================
echo  Iniciando n8n - Bot de Solicitud de Ambulancias
echo ================================================
echo.

REM Verificar que Docker está corriendo
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker no esta corriendo. Por favor inicia Docker Desktop.
    pause
    exit /b 1
)

echo [INFO] Docker esta corriendo correctamente
echo.

REM Verificar que existe el archivo .env.n8n
if not exist .env.n8n (
    echo [WARNING] No existe el archivo .env.n8n
    echo [INFO] Creando .env.n8n desde .env.n8n.example...
    copy .env.n8n.example .env.n8n
    echo [INFO] Por favor revisa y ajusta el archivo .env.n8n si es necesario
    echo.
)

REM Verificar que la red compartida existe
docker network ls | findstr recepcion-network >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARNING] La red recepcion-network no existe
    echo [INFO] Creando la red compartida...
    docker network create recepcion-network
    echo.
)

echo [INFO] Iniciando n8n con proyecto independiente 'recepcion-n8n'...
echo.

docker-compose -p recepcion-n8n --env-file .env.n8n -f docker-compose.n8n.yml up -d

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Hubo un problema al iniciar n8n
    pause
    exit /b 1
)

echo.
echo ================================================
echo  n8n iniciado correctamente!
echo ================================================
echo.
echo Accede a n8n en:
echo  URL:       http://localhost:5678
echo  Usuario:   admin
echo  Password:  admin123
echo.
echo IMPORTANTE: Cambia las credenciales en produccion!
echo.
echo Para ver los logs: docker-compose -p recepcion-n8n -f docker-compose.n8n.yml logs -f
echo Para detener:      docker-compose -p recepcion-n8n -f docker-compose.n8n.yml down
echo.
echo Proximos pasos:
echo  1. Configura credenciales de WhatsApp/Telegram en n8n
echo  2. Crea los workflows de bots
echo  3. Configura webhooks en WhatsApp/Telegram
echo.
echo Ver documentacion: n8n/README.md
echo.
pause
