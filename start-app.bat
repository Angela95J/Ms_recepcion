@echo off
REM Script para iniciar todos los servicios de la aplicación en Windows

echo ================================================
echo  Iniciando Microservicio de Recepcion
echo  PostgreSQL + Microservicio + ML Texto + ML Imagen
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

REM Verificar que existe el archivo .env
if not exist .env (
    echo [WARNING] No existe el archivo .env
    echo [INFO] Creando .env desde .env.example...
    copy .env.example .env
    echo [INFO] Por favor revisa y ajusta el archivo .env si es necesario
    echo.
)

echo [INFO] Construyendo e iniciando servicios...
echo.

docker-compose -f docker-compose.app.yml up -d --build

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Hubo un problema al iniciar los servicios
    pause
    exit /b 1
)

echo.
echo ================================================
echo  Servicios iniciados correctamente!
echo ================================================
echo.
echo Servicios disponibles:
echo  - PostgreSQL:      http://localhost:5432
echo  - Microservicio:   http://localhost:8080/api
echo  - ML Texto:        http://localhost:8001
echo  - ML Imagen:       http://localhost:8002
echo.
echo Documentacion API:
echo  - Swagger UI:      http://localhost:8080/api/swagger-ui.html
echo  - ML Texto Docs:   http://localhost:8001/docs
echo  - ML Imagen Docs:  http://localhost:8002/docs
echo.
echo Para ver los logs: docker-compose -f docker-compose.app.yml logs -f
echo Para detener:      docker-compose -f docker-compose.app.yml down
echo.
pause
