@echo off
REM Script para detener TODOS los servicios (App + n8n) en Windows

echo ================================================
echo  Deteniendo Sistema Completo
echo ================================================
echo.

echo [INFO] Deteniendo n8n...
docker-compose -p recepcion-n8n -f docker-compose.n8n.yml down

echo.
echo [INFO] Deteniendo stack principal...
docker-compose -f docker-compose.app.yml down

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Hubo un problema al detener los servicios
    pause
    exit /b 1
)

echo.
echo ================================================
echo  Todos los servicios detenidos correctamente
echo ================================================
echo.
echo Para iniciar nuevamente: start-all.bat
echo.
pause
