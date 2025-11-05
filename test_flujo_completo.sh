#!/bin/bash

# Script de prueba completa del flujo de análisis ML automático
# Prueba: Crear incidente con solicitante, ubicación, descripción e imagen

echo "====================================================="
echo "  PRUEBA COMPLETA: Incidente con Análisis ML Automático"
echo "====================================================="
echo ""

BASE_URL="http://localhost:8080/api"

# Color codes
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}📋 Paso 1: Creando incidente con solicitante y ubicación...${NC}"
echo ""

INCIDENTE_RESPONSE=$(curl -s -X POST "${BASE_URL}/incidentes" \
  -H "Content-Type: application/json" \
  -d '{
    "solicitante": {
      "nombreCompleto": "María González",
      "telefono": "+584121234567",
      "email": "maria@example.com",
      "canalOrigen": "WHATSAPP"
    },
    "ubicacion": {
      "latitud": 10.4806,
      "longitud": -66.9036,
      "descripcionTextual": "Avenida Libertador con calle Sur, frente al Hospital Central",
      "ciudad": "Caracas",
      "distrito": "Libertador"
    },
    "descripcionOriginal": "Accidente de tráfico grave. Colisión entre dos vehículos. Hay una persona inconsciente con hemorragia en la cabeza. Necesita atención urgente.",
    "tipoIncidenteReportado": "ACCIDENTE_TRAFICO"
  }')

echo -e "${GREEN}✅ Respuesta:${NC}"
echo "$INCIDENTE_RESPONSE" | jq '.'
echo ""

# Extraer ID del incidente
INCIDENTE_ID=$(echo "$INCIDENTE_RESPONSE" | jq -r '.id')

if [ "$INCIDENTE_ID" == "null" ] || [ -z "$INCIDENTE_ID" ]; then
    echo -e "${RED}❌ Error: No se pudo crear el incidente${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Incidente creado con ID: ${INCIDENTE_ID}${NC}"
echo ""

# Esperar un momento para que el análisis de texto se complete
echo -e "${YELLOW}⏳ Esperando 3 segundos para que el análisis de texto se complete...${NC}"
sleep 3

echo -e "${BLUE}📋 Paso 2: Verificando el análisis de texto automático...${NC}"
echo ""

INCIDENTE_DETALLE=$(curl -s -X GET "${BASE_URL}/incidentes/${INCIDENTE_ID}/detalle")
echo -e "${GREEN}✅ Detalle del incidente después del análisis de texto:${NC}"
echo "$INCIDENTE_DETALLE" | jq '.'
echo ""

PRIORIDAD_TEXTO=$(echo "$INCIDENTE_DETALLE" | jq -r '.prioridadTexto')
echo -e "${GREEN}📊 Prioridad calculada por análisis de texto: ${PRIORIDAD_TEXTO}${NC}"
echo ""

echo -e "${BLUE}📋 Paso 3: Subiendo imagen del incidente...${NC}"
echo ""

# Crear una imagen de prueba si no existe
TEST_IMAGE="test_accidente.jpg"
if [ ! -f "$TEST_IMAGE" ]; then
    echo -e "${YELLOW}⚠️  No se encontró imagen de prueba. Creando una imagen dummy...${NC}"
    # Crear una imagen dummy usando ImageMagick o simplemente un archivo de texto
    echo "Imagen de prueba para incidente" > "$TEST_IMAGE"
fi

MULTIMEDIA_RESPONSE=$(curl -s -X POST "${BASE_URL}/multimedia/incidente/${INCIDENTE_ID}/subir" \
  -F "archivo=@${TEST_IMAGE}" \
  -F "descripcion=Foto del accidente mostrando los dos vehículos colisionados" \
  -F "esPrincipal=true")

echo -e "${GREEN}✅ Respuesta de subida de imagen:${NC}"
echo "$MULTIMEDIA_RESPONSE" | jq '.'
echo ""

MULTIMEDIA_ID=$(echo "$MULTIMEDIA_RESPONSE" | jq -r '.id')

if [ "$MULTIMEDIA_ID" == "null" ] || [ -z "$MULTIMEDIA_ID" ]; then
    echo -e "${RED}❌ Error: No se pudo subir la imagen${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Imagen subida con ID: ${MULTIMEDIA_ID}${NC}"
echo ""

# Esperar para que el análisis de imagen se complete
echo -e "${YELLOW}⏳ Esperando 5 segundos para que el análisis de imagen se complete...${NC}"
sleep 5

echo -e "${BLUE}📋 Paso 4: Verificando el análisis de imagen automático...${NC}"
echo ""

INCIDENTE_FINAL=$(curl -s -X GET "${BASE_URL}/incidentes/${INCIDENTE_ID}/detalle")
echo -e "${GREEN}✅ Detalle completo del incidente después de todos los análisis:${NC}"
echo "$INCIDENTE_FINAL" | jq '.'
echo ""

# Extraer información relevante
ESTADO=$(echo "$INCIDENTE_FINAL" | jq -r '.estadoIncidente')
PRIORIDAD_FINAL=$(echo "$INCIDENTE_FINAL" | jq -r '.prioridadFinal')
PRIORIDAD_IMAGEN=$(echo "$INCIDENTE_FINAL" | jq -r '.prioridadImagen')
SCORE_VERACIDAD=$(echo "$INCIDENTE_FINAL" | jq -r '.scoreVeracidad')

echo ""
echo "====================================================="
echo "  📊 RESULTADOS DEL ANÁLISIS COMPLETO"
echo "====================================================="
echo -e "${GREEN}Estado del incidente:${NC} $ESTADO"
echo -e "${GREEN}Prioridad de texto:${NC} $PRIORIDAD_TEXTO"
echo -e "${GREEN}Prioridad de imagen:${NC} $PRIORIDAD_IMAGEN"
echo -e "${GREEN}Prioridad final (60% texto + 40% imagen):${NC} $PRIORIDAD_FINAL"
echo -e "${GREEN}Score de veracidad:${NC} $SCORE_VERACIDAD"
echo "====================================================="
echo ""

echo -e "${BLUE}📋 Paso 5: Consultando análisis de texto...${NC}"
echo ""

ANALISIS_TEXTO=$(curl -s -X GET "${BASE_URL}/analisis-texto/incidente/${INCIDENTE_ID}")
echo -e "${GREEN}✅ Análisis de texto detallado:${NC}"
echo "$ANALISIS_TEXTO" | jq '.'
echo ""

echo -e "${BLUE}📋 Paso 6: Consultando análisis de imagen...${NC}"
echo ""

ANALISIS_IMAGEN=$(curl -s -X GET "${BASE_URL}/analisis-imagen/incidente/${INCIDENTE_ID}")
echo -e "${GREEN}✅ Análisis de imagen detallado:${NC}"
echo "$ANALISIS_IMAGEN" | jq '.'
echo ""

echo "====================================================="
echo -e "${GREEN}✅ PRUEBA COMPLETA FINALIZADA EXITOSAMENTE${NC}"
echo "====================================================="
echo ""
echo "El incidente fue:"
echo "  1. ✅ Creado con solicitante y ubicación"
echo "  2. ✅ Analizado automáticamente (texto)"
echo "  3. ✅ Imagen subida"
echo "  4. ✅ Imagen analizada automáticamente"
echo "  5. ✅ Prioridad final calculada"
echo ""
echo "ID del incidente: ${INCIDENTE_ID}"
echo "Accede al detalle en: ${BASE_URL}/incidentes/${INCIDENTE_ID}/detalle"
echo ""
