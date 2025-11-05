$headers = @{
    'Content-Type' = 'application/json'
    'X-API-KEY' = 'admin-key-change-in-production-12345'
}

$baseUrl = 'http://localhost:8080/api/incidentes'

Write-Host "========================================"
Write-Host "Creando 10 incidentes de prueba"
Write-Host "========================================"
Write-Host ""

# Incidente 1
$body1 = @'
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
}
'@

try {
    $r1 = Invoke-RestMethod -Uri $baseUrl -Method Post -Headers $headers -Body $body1
    Write-Host "[1/10] OK - ID: $($r1.id.Substring(0,8))... Estado: $($r1.estado) Prioridad: $($r1.prioridad)"
} catch {
    Write-Host "[1/10] ERROR: $($_.Exception.Message)"
}

# Incidente 2
$body2 = @'
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
}
'@

try {
    $r2 = Invoke-RestMethod -Uri $baseUrl -Method Post -Headers $headers -Body $body2
    Write-Host "[2/10] OK - ID: $($r2.id.Substring(0,8))... Estado: $($r2.estado) Prioridad: $($r2.prioridad)"
} catch {
    Write-Host "[2/10] ERROR: $($_.Exception.Message)"
}

# Incidente 3
$body3 = @'
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
}
'@

try {
    $r3 = Invoke-RestMethod -Uri $baseUrl -Method Post -Headers $headers -Body $body3
    Write-Host "[3/10] OK - ID: $($r3.id.Substring(0,8))... Estado: $($r3.estado) Prioridad: $($r3.prioridad)"
} catch {
    Write-Host "[3/10] ERROR: $($_.Exception.Message)"
}

# Incidente 4
$body4 = @'
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
}
'@

try {
    $r4 = Invoke-RestMethod -Uri $baseUrl -Method Post -Headers $headers -Body $body4
    Write-Host "[4/10] OK - ID: $($r4.id.Substring(0,8))... Estado: $($r4.estado) Prioridad: $($r4.prioridad)"
} catch {
    Write-Host "[4/10] ERROR: $($_.Exception.Message)"
}

# Incidente 5
$body5 = @'
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
}
'@

try {
    $r5 = Invoke-RestMethod -Uri $baseUrl -Method Post -Headers $headers -Body $body5
    Write-Host "[5/10] OK - ID: $($r5.id.Substring(0,8))... Estado: $($r5.estado) Prioridad: $($r5.prioridad)"
} catch {
    Write-Host "[5/10] ERROR: $($_.Exception.Message)"
}

# Incidente 6
$body6 = @'
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
}
'@

try {
    $r6 = Invoke-RestMethod -Uri $baseUrl -Method Post -Headers $headers -Body $body6
    Write-Host "[6/10] OK - ID: $($r6.id.Substring(0,8))... Estado: $($r6.estado) Prioridad: $($r6.prioridad)"
} catch {
    Write-Host "[6/10] ERROR: $($_.Exception.Message)"
}

# Incidente 7
$body7 = @'
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
}
'@

try {
    $r7 = Invoke-RestMethod -Uri $baseUrl -Method Post -Headers $headers -Body $body7
    Write-Host "[7/10] OK - ID: $($r7.id.Substring(0,8))... Estado: $($r7.estado) Prioridad: $($r7.prioridad)"
} catch {
    Write-Host "[7/10] ERROR: $($_.Exception.Message)"
}

# Incidente 8
$body8 = @'
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
}
'@

try {
    $r8 = Invoke-RestMethod -Uri $baseUrl -Method Post -Headers $headers -Body $body8
    Write-Host "[8/10] OK - ID: $($r8.id.Substring(0,8))... Estado: $($r8.estado) Prioridad: $($r8.prioridad)"
} catch {
    Write-Host "[8/10] ERROR: $($_.Exception.Message)"
}

# Incidente 9
$body9 = @'
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
}
'@

try {
    $r9 = Invoke-RestMethod -Uri $baseUrl -Method Post -Headers $headers -Body $body9
    Write-Host "[9/10] OK - ID: $($r9.id.Substring(0,8))... Estado: $($r9.estado) Prioridad: $($r9.prioridad)"
} catch {
    Write-Host "[9/10] ERROR: $($_.Exception.Message)"
}

# Incidente 10
$body10 = @'
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
'@

try {
    $r10 = Invoke-RestMethod -Uri $baseUrl -Method Post -Headers $headers -Body $body10
    Write-Host "[10/10] OK - ID: $($r10.id.Substring(0,8))... Estado: $($r10.estado) Prioridad: $($r10.prioridad)"
} catch {
    Write-Host "[10/10] ERROR: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "========================================"
Write-Host "Proceso completado"
Write-Host "========================================"
