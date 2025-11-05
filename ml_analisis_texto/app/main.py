from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import uvicorn
from datetime import datetime

from app.config import settings
from app.schemas.analisis_schema import (
    AnalizarTextoRequest,
    AnalizarTextoResponse,
    HealthCheckResponse,
    ReentrenarRequest,
    ReentrenarResponse
)
from app.services.analisis_service import analisis_service


# Crear aplicación FastAPI
app = FastAPI(
    title="API de Análisis de Texto - ML",
    description="Servicio de Machine Learning para análisis de texto de incidentes usando K-means",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Configurar CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # En producción, especificar orígenes permitidos
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/", tags=["Root"])
async def root():
    """Endpoint raíz"""
    return {
        "service": "ML Análisis de Texto",
        "version": "1.0.0",
        "status": "running",
        "timestamp": datetime.now().isoformat()
    }


@app.get("/api/ml/salud", response_model=HealthCheckResponse, tags=["Health"])
async def health_check():
    """
    Health check del servicio

    Verifica:
    - Estado del servicio
    - Si el modelo está cargado
    - Versión del modelo
    """
    try:
        health_data = analisis_service.health_check()

        return HealthCheckResponse(
            status=health_data["status"],
            model_loaded=health_data["model_loaded"],
            model_version=health_data["model_version"],
            timestamp=health_data["timestamp"]
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"Error en health check: {str(e)}"
        )


@app.get("/api/ml/modelo/info", tags=["Model"])
async def get_model_info():
    """
    Obtiene información del modelo cargado

    Returns:
        - Estado de carga
        - Versión
        - Número de clusters
        - Mapeo de clusters a prioridades
        - Etiquetas de clusters
    """
    try:
        return analisis_service.get_model_info()
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error al obtener información del modelo: {str(e)}"
        )


@app.post(
    "/api/ml/analizar-texto",
    response_model=AnalizarTextoResponse,
    status_code=status.HTTP_200_OK,
    tags=["Análisis"]
)
async def analizar_texto(request: AnalizarTextoRequest):
    """
    Analiza el texto de un incidente y retorna análisis completo

    Args:
        request: Contiene el texto a analizar y opcionalmente el ID del incidente

    Returns:
        AnalizarTextoResponse con:
        - Prioridad calculada (1-5)
        - Nivel de gravedad (1-5)
        - Tipo de incidente predicho
        - Categorías detectadas
        - Palabras clave críticas
        - Entidades médicas
        - Score de confianza
        - Probabilidades por categoría
        - Metadata del modelo

    Raises:
        400: Si el texto es inválido
        503: Si el modelo no está cargado
        500: Si ocurre un error inesperado
    """
    try:
        # Validar que el modelo esté cargado
        if not analisis_service.model_loaded:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="El modelo no está entrenado. Por favor entrena el modelo primero llamando a /api/ml/reentrenar"
            )

        # Validar texto
        if not request.texto or len(request.texto.strip()) < 10:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="El texto debe tener al menos 10 caracteres"
            )

        # Realizar análisis
        resultado = analisis_service.analizar_texto(
            texto=request.texto,
            incidente_id=str(request.incidente_id) if request.incidente_id else None
        )

        return resultado

    except HTTPException:
        raise
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error inesperado al analizar texto: {str(e)}"
        )


@app.post(
    "/api/ml/reentrenar",
    response_model=ReentrenarResponse,
    status_code=status.HTTP_200_OK,
    tags=["Model"]
)
async def reentrenar_modelo(request: ReentrenarRequest):
    """
    Reentrena el modelo con datos de la base de datos

    NOTA: Esta es una versión simplificada. En producción, esto debería:
    - Requerir autenticación de admin
    - Ejecutarse en background (Celery, etc.)
    - Usar datos históricos de la BD

    Args:
        request: Parámetros de entrenamiento

    Returns:
        ReentrenarResponse con resultados del entrenamiento
    """
    try:
        # TODO: Implementar reentrenamiento con datos de BD
        # Por ahora retornamos un placeholder

        return ReentrenarResponse(
            success=False,
            message="Reentrenamiento no implementado. Requiere datos históricos de incidentes.",
            samples_used=0,
            clusters_created=0,
            model_version=settings.model_version,
            metrics=None
        )

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error al reentrenar modelo: {str(e)}"
        )


@app.exception_handler(404)
async def not_found_handler(request, exc):
    """Handler para rutas no encontradas"""
    return JSONResponse(
        status_code=404,
        content={
            "detail": "Endpoint no encontrado",
            "path": str(request.url),
            "method": request.method
        }
    )


@app.exception_handler(500)
async def internal_error_handler(request, exc):
    """Handler para errores internos"""
    return JSONResponse(
        status_code=500,
        content={
            "detail": "Error interno del servidor",
            "type": type(exc).__name__,
            "message": str(exc)
        }
    )


if __name__ == "__main__":
    print(f"""
    ╔════════════════════════════════════════════════════════════╗
    ║  ML Análisis de Texto - Servicio de Machine Learning      ║
    ║  Puerto: {settings.port}                                           ║
    ║  Docs: http://{settings.host}:{settings.port}/docs                    ║
    ╚════════════════════════════════════════════════════════════╝
    """)

    uvicorn.run(
        "app.main:app",
        host=settings.host,
        port=settings.port,
        reload=settings.debug,
        log_level="info"
    )
