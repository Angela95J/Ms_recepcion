@echo off
REM Script para detener todos los servicios de la aplicación en Windows

echo ================================================
echo  Deteniendo Microservicio de Recepcion
echo ================================================
echo.

docker-compose -f docker-compose.app.yml down

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Hubo un problema al detener los servicios
    pause
    exit /b 1
)

echo.
echo [INFO] Servicios detenidos correctamente
echo.
pause
