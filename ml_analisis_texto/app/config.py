from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """Configuración de la aplicación"""

    # Servidor
    host: str = "0.0.0.0"
    port: int = 8001
    debug: bool = True

    # Base de datos
    db_host: str = "localhost"
    db_port: int = 5432
    db_name: str = "recepcion_db"
    db_user: str = "postgres"
    db_password: str = "postgres"

    # Modelo
    model_version: str = "kmeans-v1.0"
    num_clusters: int = 5
    min_confidence: float = 0.6

    # Rutas
    model_path: str = "./trained_models/kmeans_texto_model.pkl"
    vectorizer_path: str = "./trained_models/tfidf_vectorizer.pkl"

    class Config:
        env_file = ".env"
        case_sensitive = False

    @property
    def database_url(self) -> str:
        """Construye la URL de conexión a PostgreSQL"""
        return f"postgresql://{self.db_user}:{self.db_password}@{self.db_host}:{self.db_port}/{self.db_name}"


settings = Settings()
