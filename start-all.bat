@echo off
REM Script para iniciar TODOS los servicios (App + n8n) en Windows

echo ================================================
echo  Iniciando Sistema Completo de Recepcion
echo  Microservicio + ML + PostgreSQL + n8n
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

REM ====================================
REM PASO 1: Iniciar Stack Principal
REM ====================================
echo ================================================
echo  PASO 1/2: Iniciando Stack Principal
echo  (PostgreSQL + Microservicio + ML)
echo ================================================
echo.

call start-app.bat
if %errorlevel% neq 0 (
    echo [ERROR] Fallo al iniciar el stack principal
    pause
    exit /b 1
)

echo.
echo [INFO] Stack principal iniciado correctamente
echo.

REM Esperar 10 segundos para que los servicios estén listos
echo [INFO] Esperando 10 segundos para que los servicios se estabilicen...
timeout /t 10 /nobreak >nul

REM ====================================
REM PASO 2: Iniciar n8n
REM ====================================
echo ================================================
echo  PASO 2/2: Iniciando n8n
echo ================================================
echo.

call start-n8n.bat
if %errorlevel% neq 0 (
    echo [ERROR] Fallo al iniciar n8n
    pause
    exit /b 1
)

echo.
echo ================================================
echo  SISTEMA COMPLETO INICIADO!
echo ================================================
echo.
echo Servicios disponibles:
echo.
echo  BACKEND:
echo   - PostgreSQL:      localhost:5432
echo   - Microservicio:   http://localhost:8080/api
echo   - Swagger UI:      http://localhost:8080/api/swagger-ui.html
echo   - ML Texto:        http://localhost:8001
echo   - ML Imagen:       http://localhost:8002
echo.
echo  BOT / AUTOMATIZACION:
echo   - n8n:             http://localhost:5678
echo   - Usuario:         admin
echo   - Password:        admin123
echo.
echo Comandos utiles:
echo   Ver logs app:      docker-compose -f docker-compose.app.yml logs -f
echo   Ver logs n8n:      docker-compose -f docker-compose.n8n.yml logs -f
echo   Detener todo:      stop-all.bat
echo.
echo Proximos pasos:
echo   1. Abre n8n: http://localhost:5678
echo   2. Configura credenciales de WhatsApp/Telegram
echo   3. Crea workflows de bots (ver n8n/README.md)
echo   4. Prueba el flujo completo
echo.
pause
