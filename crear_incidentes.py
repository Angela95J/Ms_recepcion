import requests
import json

BASE_URL = "http://localhost:8080/api/incidentes"
API_KEY = "admin-key-change-in-production-12345"

headers = {
    "Content-Type": "application/json",
    "X-API-KEY": API_KEY
}

incidentes = [
    {
        "solicitante": {
            "nombreCompleto": "Juan Perez Garcia",
            "telefono": "+59177123456",
            "canalOrigen": "WHATSAPP"
        },
        "ubicacion": {
            "descripcionTextual": "Av. Cristo Redentor y 4to Anillo",
            "latitud": -17.783,
            "longitud": -63.182,
            "ciudad": "Santa Cruz",
            "distrito": "Zona Norte"
        },
        "descripcionOriginal": "Accidente de transito grave persona con sangrado abundante",
        "tipoIncidenteReportado": "ACCIDENTE_TRAFICO"
    },
    {
        "solicitante": {
            "nombreCompleto": "Maria Lopez Fernandez",
            "telefono": "+59176234567",
            "canalOrigen": "TELEGRAM"
        },
        "ubicacion": {
            "descripcionTextual": "Plaza 24 de Septiembre",
            "latitud": -17.7833,
            "longitud": -63.1821
        },
        "descripcionOriginal": "Emergencia cardiaca, persona con dolor intenso en el pecho",
        "tipoIncidenteReportado": "EMERGENCIA_MEDICA"
    },
    {
        "solicitante": {
            "nombreCompleto": "Carlos Rodriguez",
            "telefono": "+59175345678",
            "canalOrigen": "WHATSAPP"
        },
        "ubicacion": {
            "descripcionTextual": "Mercado La Ramada",
            "latitud": -17.7850,
            "longitud": -63.1810
        },
        "descripcionOriginal": "Persona desmayada en el mercado parece tener convulsiones",
        "tipoIncidenteReportado": "EMERGENCIA_MEDICA"
    },
    {
        "solicitante": {
            "nombreCompleto": "Ana Martinez",
            "telefono": "+59174456789",
            "canalOrigen": "TELEGRAM"
        },
        "ubicacion": {
            "descripcionTextual": "Terminal de Buses",
            "latitud": -17.7900,
            "longitud": -63.1700
        },
        "descripcionOriginal": "Nino de 5 anos con quemaduras en el brazo por agua caliente",
        "tipoIncidenteReportado": "EMERGENCIA_MEDICA"
    },
    {
        "solicitante": {
            "nombreCompleto": "Roberto Silva",
            "telefono": "+59173567890",
            "canalOrigen": "WHATSAPP"
        },
        "ubicacion": {
            "descripcionTextual": "Av. Banzer y 3er Anillo",
            "latitud": -17.7750,
            "longitud": -63.1900
        },
        "descripcionOriginal": "Choque multiple entre 3 vehiculos varios heridos con lesiones menores",
        "tipoIncidenteReportado": "ACCIDENTE_TRAFICO"
    },
    {
        "solicitante": {
            "nombreCompleto": "Laura Gutierrez",
            "telefono": "+59172678901",
            "canalOrigen": "TELEGRAM"
        },
        "ubicacion": {
            "descripcionTextual": "Universidad UAGRM",
            "latitud": -17.7840,
            "longitud": -63.1805
        },
        "descripcionOriginal": "Estudiante con reaccion alergica severa dificultad para respirar",
        "tipoIncidenteReportado": "EMERGENCIA_MEDICA"
    },
    {
        "solicitante": {
            "nombreCompleto": "Pedro Sanchez",
            "telefono": "+59171789012",
            "canalOrigen": "WHATSAPP"
        },
        "ubicacion": {
            "descripcionTextual": "Parque El Arenal",
            "latitud": -17.7820,
            "longitud": -63.1825
        },
        "descripcionOriginal": "Adulto mayor con caida posible fractura de cadera dolor intenso",
        "tipoIncidenteReportado": "EMERGENCIA_MEDICA"
    },
    {
        "solicitante": {
            "nombreCompleto": "Sofia Ramirez",
            "telefono": "+59170890123",
            "canalOrigen": "TELEGRAM"
        },
        "ubicacion": {
            "descripcionTextual": "Av. Roca y Coronado",
            "latitud": -17.7880,
            "longitud": -63.1750
        },
        "descripcionOriginal": "Mujer embarazada con contracciones fuertes y sangrado",
        "tipoIncidenteReportado": "EMERGENCIA_MEDICA"
    },
    {
        "solicitante": {
            "nombreCompleto": "Diego Torres",
            "telefono": "+59179901234",
            "canalOrigen": "WHATSAPP"
        },
        "ubicacion": {
            "descripcionTextual": "Estadio Ramon Tahuichi Aguilera",
            "latitud": -17.7700,
            "longitud": -63.1950
        },
        "descripcionOriginal": "Deportista con lesion grave en rodilla durante partido de futbol",
        "tipoIncidenteReportado": "EMERGENCIA_MEDICA"
    },
    {
        "solicitante": {
            "nombreCompleto": "Valentina Cruz",
            "telefono": "+59178012345",
            "canalOrigen": "TELEGRAM"
        },
        "ubicacion": {
            "descripcionTextual": "Av. San Martin y 2do Anillo",
            "latitud": -17.7790,
            "longitud": -63.1870
        },
        "descripcionOriginal": "Accidente de motocicleta conductor inconsciente sangrado en la cara",
        "tipoIncidenteReportado": "ACCIDENTE_TRAFICO"
    }
]

print("="*50)
print("  Creando 10 incidentes de prueba")
print("="*50)
print()

exitosos = 0
fallidos = 0

for i, incidente in enumerate(incidentes, 1):
    try:
        response = requests.post(BASE_URL, headers=headers, json=incidente, timeout=10)

        if response.status_code == 201:
            data = response.json()
            incidente_id = data.get('id', 'N/A')
            estado = data.get('estado', 'N/A')
            prioridad = data.get('prioridad', 'N/A')
            if incidente_id != 'N/A':
                print(f"[{i}/10] OK - ID: {incidente_id[:8]}... Estado: {estado} Prioridad: {prioridad}")
            else:
                print(f"[{i}/10] OK - Creado (respuesta: {str(data)[:100]})")
            exitosos += 1
        else:
            print(f"[{i}/10] ERROR {response.status_code}: {response.text[:200]}")
            fallidos += 1
    except Exception as e:
        print(f"[{i}/10] ERROR: {type(e).__name__}: {str(e)}")
        if hasattr(e, 'response') and e.response is not None:
            print(f"    Response: {e.response.text[:200]}")
        fallidos += 1

print()
print("="*50)
print(f"  Resumen: {exitosos} exitosos, {fallidos} fallidos")
print("="*50)
