import re
import unicodedata
from typing import List, Set
import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.stem import SnowballStemmer


class TextPreprocessor:
    """Preprocesamiento de texto en español para análisis de incidentes"""

    def __init__(self):
        """Inicializa el preprocesador"""
        # Descargar recursos de NLTK si no existen
        try:
            nltk.data.find('tokenizers/punkt')
        except LookupError:
            nltk.download('punkt')

        try:
            nltk.data.find('tokenizers/punkt_tab')
        except LookupError:
            nltk.download('punkt_tab')

        try:
            nltk.data.find('corpora/stopwords')
        except LookupError:
            nltk.download('stopwords')

        # Stopwords en español
        self.stop_words: Set[str] = set(stopwords.words('spanish'))

        # Palabras que NO deben eliminarse aunque sean stopwords (contexto médico/emergencia)
        self.keep_words: Set[str] = {
            'sangre', 'dolor', 'grave', 'urgente', 'rapido', 'ayuda',
            'no', 'sin', 'muy', 'poco', 'mucho', 'mas', 'menos'
        }

        # Actualizar stopwords
        self.stop_words = self.stop_words - self.keep_words

        # Stemmer para español
        self.stemmer = SnowballStemmer('spanish')

        # Palabras clave críticas que indican urgencia
        self.critical_keywords: Set[str] = {
            # Condiciones críticas
            'inconsciente', 'desmayado', 'infarto', 'paro', 'cardiaco', 'respiratorio',
            'hemorragia', 'sangre', 'sangrando', 'herido', 'fractura', 'quemadura',

            # Urgencias vitales
            'no respira', 'no responde', 'atrapado', 'ahogando', 'asfixia',
            'convulsion', 'convulsionando', 'ataque', 'epileptico',

            # Trauma
            'accidente', 'choque', 'colision', 'atropellado', 'caida',
            'golpe', 'trauma', 'lesion', 'cortado',

            # Condiciones médicas
            'embarazada', 'parto', 'bebe', 'nino', 'anciano',
            'diabetico', 'alergico', 'medicamento',

            # Urgencia explícita
            'urgente', 'emergencia', 'grave', 'critico', 'rapido', 'inmediato',
            'ayuda', 'socorro', 'ambulancia', 'hospital',

            # Múltiples víctimas
            'varios', 'muchos', 'heridos', 'personas', 'victimas'
        }

    def clean_text(self, text: str) -> str:
        """Limpia el texto básico"""
        # Convertir a minúsculas
        text = text.lower()

        # Eliminar URLs
        text = re.sub(r'http\S+|www\S+', '', text)

        # Eliminar emails
        text = re.sub(r'\S+@\S+', '', text)

        # Eliminar números de teléfono (patrones comunes)
        text = re.sub(r'\+?\d{1,4}?[-.\s]?\(?\d{1,3}?\)?[-.\s]?\d{1,4}[-.\s]?\d{1,4}[-.\s]?\d{1,9}', '', text)

        # Normalizar caracteres unicode
        text = unicodedata.normalize('NFKD', text).encode('ascii', 'ignore').decode('utf-8')

        # Eliminar caracteres especiales pero mantener espacios y puntos
        text = re.sub(r'[^a-z0-9\s.]', ' ', text)

        # Eliminar múltiples espacios
        text = re.sub(r'\s+', ' ', text)

        return text.strip()

    def tokenize(self, text: str) -> List[str]:
        """Tokeniza el texto"""
        return word_tokenize(text, language='spanish')

    def remove_stopwords(self, tokens: List[str]) -> List[str]:
        """Elimina stopwords pero mantiene palabras críticas"""
        return [token for token in tokens if token not in self.stop_words or len(token) > 2]

    def stem_tokens(self, tokens: List[str]) -> List[str]:
        """Aplica stemming a los tokens"""
        return [self.stemmer.stem(token) for token in tokens]

    def extract_critical_keywords(self, text: str) -> List[str]:
        """Extrae palabras clave críticas del texto"""
        text_lower = text.lower()
        found_keywords = []

        for keyword in self.critical_keywords:
            if keyword in text_lower:
                found_keywords.append(keyword)

        return found_keywords

    def preprocess(self, text: str, apply_stemming: bool = True) -> str:
        """Pipeline completo de preprocesamiento"""
        # 1. Limpiar texto
        cleaned = self.clean_text(text)

        # 2. Tokenizar
        tokens = self.tokenize(cleaned)

        # 3. Eliminar stopwords
        tokens = self.remove_stopwords(tokens)

        # 4. Aplicar stemming si se solicita
        if apply_stemming:
            tokens = self.stem_tokens(tokens)

        # 5. Reconstruir texto
        return ' '.join(tokens)

    def extract_medical_entities(self, text: str) -> dict:
        """Extrae entidades médicas del texto (versión simple basada en reglas)"""
        text_lower = text.lower()

        entities = {
            "sintomas": [],
            "partes_cuerpo": [],
            "condiciones": [],
            "vehiculos": []
        }

        # Síntomas comunes
        sintomas = ['dolor', 'sangrado', 'hemorragia', 'fractura', 'quemadura', 'herida',
                   'mareo', 'nausea', 'vomito', 'fiebre', 'inconsciente', 'desmayo',
                   'convulsion', 'asfixia', 'ahogo', 'paro']

        # Partes del cuerpo
        partes_cuerpo = ['cabeza', 'cuello', 'torax', 'pecho', 'abdomen', 'brazo', 'pierna',
                        'mano', 'pie', 'espalda', 'columna', 'cara', 'ojo', 'nariz']

        # Condiciones especiales
        condiciones = ['embarazada', 'embarazo', 'diabetico', 'diabetes', 'hipertenso',
                      'cardiaco', 'asmatico', 'epileptico', 'alergico']

        # Vehículos
        vehiculos = ['auto', 'carro', 'vehiculo', 'camion', 'bus', 'moto', 'motocicleta',
                    'bicicleta', 'taxi', 'colectivo']

        # Buscar síntomas
        for sintoma in sintomas:
            if sintoma in text_lower:
                entities["sintomas"].append(sintoma)

        # Buscar partes del cuerpo
        for parte in partes_cuerpo:
            if parte in text_lower:
                entities["partes_cuerpo"].append(parte)

        # Buscar condiciones
        for condicion in condiciones:
            if condicion in text_lower:
                entities["condiciones"].append(condicion)

        # Buscar vehículos
        for vehiculo in vehiculos:
            if vehiculo in text_lower:
                entities["vehiculos"].append(vehiculo)

        # Filtrar listas vacías
        return {k: v for k, v in entities.items() if v}
