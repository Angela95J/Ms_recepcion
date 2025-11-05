import cv2
import numpy as np
from PIL import Image
from typing import Tuple, Dict
from pathlib import Path


class ImagePreprocessor:
    """Preprocesamiento de imágenes para análisis ML"""

    def __init__(self, target_size: Tuple[int, int] = (224, 224)):
        """
        Inicializa el preprocesador

        Args:
            target_size: Tamaño objetivo (ancho, alto)
        """
        self.target_size = target_size

    def load_image(self, image_path: str) -> np.ndarray:
        """Carga una imagen desde archivo"""
        if not Path(image_path).exists():
            raise FileNotFoundError(f"Imagen no encontrada: {image_path}")

        # Cargar con OpenCV
        img = cv2.imread(image_path)
        if img is None:
            raise ValueError(f"No se pudo cargar la imagen: {image_path}")

        # Convertir de BGR a RGB
        img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        return img

    def resize_image(self, img: np.ndarray, size: Tuple[int, int] = None) -> np.ndarray:
        """Redimensiona imagen manteniendo aspect ratio"""
        if size is None:
            size = self.target_size

        return cv2.resize(img, size, interpolation=cv2.INTER_AREA)

    def normalize_image(self, img: np.ndarray) -> np.ndarray:
        """Normaliza valores de píxeles a [0, 1]"""
        return img.astype(np.float32) / 255.0

    def get_image_quality(self, img: np.ndarray) -> str:
        """Evalúa la calidad de la imagen"""
        # Calcular nitidez usando Laplaciano
        gray = cv2.cvtColor(img, cv2.COLOR_RGB2GRAY)
        laplacian_var = cv2.Laplacian(gray, cv2.CV_64F).var()

        # Calcular brillo promedio
        brightness = np.mean(gray)

        # Determinar calidad
        if laplacian_var > 500 and 50 < brightness < 200:
            return "EXCELENTE"
        elif laplacian_var > 200 and 30 < brightness < 220:
            return "BUENA"
        elif laplacian_var > 50:
            return "REGULAR"
        else:
            return "MALA"

    def is_image_clear(self, img: np.ndarray) -> bool:
        """Verifica si la imagen es clara (no borrosa)"""
        gray = cv2.cvtColor(img, cv2.COLOR_RGB2GRAY)
        laplacian_var = cv2.Laplacian(gray, cv2.CV_64F).var()
        return laplacian_var > 100

    def extract_features(self, img: np.ndarray) -> np.ndarray:
        """
        Extrae características visuales de la imagen

        Usa histogramas de color, textura y bordes
        """
        # Redimensionar
        img_resized = self.resize_image(img)

        features = []

        # 1. Histograma de color (RGB)
        for i in range(3):
            hist = cv2.calcHist([img_resized], [i], None, [32], [0, 256])
            features.extend(hist.flatten())

        # 2. Textura (gris)
        gray = cv2.cvtColor(img_resized, cv2.COLOR_RGB2GRAY)

        # Calcular gradientes
        sobelx = cv2.Sobel(gray, cv2.CV_64F, 1, 0, ksize=3)
        sobely = cv2.Sobel(gray, cv2.CV_64F, 0, 1, ksize=3)

        # Magnitud de gradientes
        gradient_magnitude = np.sqrt(sobelx**2 + sobely**2)
        gradient_hist = np.histogram(gradient_magnitude, bins=32)[0]
        features.extend(gradient_hist)

        # 3. Estadísticas de color
        for i in range(3):
            channel = img_resized[:, :, i]
            features.extend([
                np.mean(channel),
                np.std(channel),
                np.median(channel),
                np.min(channel),
                np.max(channel)
            ])

        # 4. Características adicionales de textura/bordes
        edges = cv2.Canny(gray, 50, 150)
        features.extend([
            np.mean(edges),
            np.std(edges),
            np.sum(edges > 0) / edges.size,  # Densidad de bordes
            np.mean(sobelx),
            np.std(sobelx),
            np.mean(sobely),
            np.std(sobely)
        ])

        return np.array(features)

    def detect_simple_objects(self, img: np.ndarray) -> Dict[str, int]:
        """
        Detección simple de objetos usando técnicas clásicas
        (En producción, usar YOLO o similar)
        """
        gray = cv2.cvtColor(img, cv2.COLOR_RGB2GRAY)

        # Detección de bordes
        edges = cv2.Canny(gray, 50, 150)

        # Contornos
        contours, _ = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

        # Filtrar contornos por área
        significant_contours = [c for c in contours if cv2.contourArea(c) > 500]

        # Estimación aproximada
        num_objects = len(significant_contours)

        return {
            "total_objetos": num_objects,
            "objetos_grandes": sum(1 for c in significant_contours if cv2.contourArea(c) > 5000),
            "objetos_medianos": sum(1 for c in significant_contours if 1000 < cv2.contourArea(c) <= 5000)
        }

    def get_image_info(self, img: np.ndarray) -> Dict:
        """Obtiene información de la imagen"""
        height, width = img.shape[:2]

        return {
            "width": width,
            "height": height,
            "resolution": f"{width}x{height}",
            "channels": img.shape[2] if len(img.shape) == 3 else 1,
            "size_bytes": img.nbytes
        }

    def preprocess_for_model(self, image_path: str) -> np.ndarray:
        """Pipeline completo de preprocesamiento"""
        # 1. Cargar imagen
        img = self.load_image(image_path)

        # 2. Redimensionar
        img = self.resize_image(img)

        # 3. Normalizar
        img = self.normalize_image(img)

        # 4. Extraer características
        features = self.extract_features((img * 255).astype(np.uint8))

        return features
