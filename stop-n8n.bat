@echo off
REM Script para detener n8n en Windows

echo ================================================
echo  Deteniendo n8n
echo ================================================
echo.

docker-compose -p recepcion-n8n -f docker-compose.n8n.yml down

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Hubo un problema al detener n8n
    pause
    exit /b 1
)

echo.
echo [INFO] n8n detenido correctamente
echo.
pause
