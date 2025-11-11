@echo off
REM Script para ver los logs de n8n en Windows

echo ================================================
echo  Logs de n8n
echo  Presiona Ctrl+C para salir
echo ================================================
echo.

docker-compose -p recepcion-n8n -f docker-compose.n8n.yml logs -f
