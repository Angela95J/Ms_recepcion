# Imagen base de Python optimizada
FROM python:3.11-slim

WORKDIR /app

# Instalar dependencias del sistema necesarias para NLTK y scikit-learn
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

# Copiar requirements primero (para aprovechar cache de Docker)
COPY requirements.txt .

# Instalar dependencias de Python
RUN pip install --no-cache-dir -r requirements.txt

# Descargar datos de NLTK necesarios
RUN python -m nltk.downloader -d /usr/local/share/nltk_data \
    punkt \
    stopwords \
    wordnet \
    omw-1.4

# Copiar el código de la aplicación
COPY app/ ./app/

# Copiar modelos entrenados si existen
RUN mkdir -p trained_models
COPY trained_models/ ./trained_models/

# Crear directorio para datos si no existe
RUN mkdir -p /app/data

# Crear usuario no-root por seguridad
RUN useradd -m -u 1000 mluser && chown -R mluser:mluser /app
USER mluser

# Exponer el puerto de FastAPI
EXPOSE 8001

# Variables de entorno
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONTWRITEBYTECODE=1
ENV PORT=8001

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD python -c "import urllib.request; urllib.request.urlopen('http://localhost:8001/api/ml/salud')" || exit 1

# Comando de inicio
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]
