from fastapi import FastAPI, HTTPException, status, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import uvicorn
from datetime import datetime
from pathlib import Path
import shutil

from app.config import settings
from app.schemas.analisis_schema import (
    AnalizarImagenRequest,
    AnalizarImagenResponse,
    HealthCheckResponse
)
from app.services.analisis_service import analisis_service


app = FastAPI(
    title="API de Análisis de Imágenes - ML",
    description="Servicio de Machine Learning para análisis de imágenes usando K-means",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
async def root():
    """Endpoint raíz"""
    return {
        "service": "ML Análisis de Imágenes",
        "version": "1.0.0",
        "status": "running",
        "timestamp": datetime.now().isoformat()
    }


@app.get("/api/ml/salud", response_model=HealthCheckResponse)
async def health_check():
    """Health check del servicio"""
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


@app.post("/api/ml/analizar-imagen", response_model=AnalizarImagenResponse)
async def analizar_imagen(request: AnalizarImagenRequest):
    """Analiza una imagen desde una ruta de archivo"""
    try:
        if not analisis_service.model_loaded:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Modelo no entrenado"
            )

        if not Path(request.imagen_path).exists():
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Imagen no encontrada: {request.imagen_path}"
            )

        resultado = analisis_service.analizar_imagen(
            imagen_path=request.imagen_path,
            multimedia_id=str(request.multimedia_id) if request.multimedia_id else None,
            incidente_id=str(request.incidente_id) if request.incidente_id else None
        )

        return resultado

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error al analizar imagen: {str(e)}"
        )


@app.post("/api/ml/analizar-imagen-upload", response_model=AnalizarImagenResponse)
async def analizar_imagen_upload(file: UploadFile = File(...)):
    """Analiza una imagen subida directamente"""
    try:
        if not analisis_service.model_loaded:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Modelo no entrenado"
            )

        # Validar formato
        file_ext = file.filename.split('.')[-1].lower()
        if file_ext not in settings.allowed_formats:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Formato no soportado. Permitidos: {settings.allowed_formats}"
            )

        # Guardar temporalmente
        temp_dir = Path("./temp")
        temp_dir.mkdir(exist_ok=True)
        temp_path = temp_dir / file.filename

        with temp_path.open("wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # Analizar
        resultado = analisis_service.analizar_imagen(imagen_path=str(temp_path))

        # Limpiar
        temp_path.unlink()

        return resultado

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error al analizar imagen: {str(e)}"
        )


if __name__ == "__main__":
    print(f"""
    ╔════════════════════════════════════════════════════════════╗
    ║  ML Análisis de Imágenes - Machine Learning Service       ║
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
