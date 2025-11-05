from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    """Configuración de la aplicación"""

    # Servidor
    host: str = "0.0.0.0"
    port: int = 8002
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

    # Imágenes
    max_image_size_mb: int = 10
    supported_formats: str = "jpg,jpeg,png,bmp"
    image_resize_width: int = 224
    image_resize_height: int = 224

    # Rutas
    model_path: str = "./trained_models/kmeans_imagen_model.pkl"
    feature_extractor_path: str = "./trained_models/feature_extractor.pkl"

    class Config:
        env_file = ".env"
        case_sensitive = False

    @property
    def database_url(self) -> str:
        """Construye la URL de conexión a PostgreSQL"""
        return f"postgresql://{self.db_user}:{self.db_password}@{self.db_host}:{self.db_port}/{self.db_name}"

    @property
    def allowed_formats(self) -> List[str]:
        """Retorna lista de formatos permitidos"""
        return self.supported_formats.split(',')

    @property
    def max_image_size_bytes(self) -> int:
        """Retorna tamaño máximo en bytes"""
        return self.max_image_size_mb * 1024 * 1024


settings = Settings()
