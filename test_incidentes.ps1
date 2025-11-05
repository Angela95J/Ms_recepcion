# Script PowerShell para probar el microservicio con 10 incidentes

$baseUrl = "http://localhost:8080/api"
$apiKey = "admin-key-change-in-production-12345"
$headers = @{
    "Content-Type" = "application/json"
    "X-API-KEY" = $apiKey
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Probando Microservicio de Recepción" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Lista de 10 incidentes de prueba
$incidentes = @(
    @{
        solicitante = @{
            nombreCompleto = "Juan Pérez García"
            telefono = "+59177123456"
            canalOrigen = "WHATSAPP"
        }
        ubicacion = @{
            descripcionTextual = "Av. Cristo Redentor y 4to Anillo"
            latitud = -17.783
            longitud = -63.182
            ciudad = "Santa Cruz de la Sierra"
            distrito = "Zona Norte"
        }
        descripcionOriginal = "Accidente de tránsito grave, persona con sangrado abundante en la cabeza y fractura aparente en pierna derecha"
        tipoIncidenteReportado = "ACCIDENTE_TRAFICO"
    },
    @{
        solicitante = @{
            nombreCompleto = "María López Fernández"
            telefono = "+59176234567"
            canalOrigen = "TELEGRAM"
        }
        ubicacion = @{
            descripcionTextual = "Plaza 24 de Septiembre"
            latitud = -17.7833
            longitud = -63.1821
        }
        descripcionOriginal = "Emergencia cardíaca, persona con dolor intenso en el pecho, dificultad para respirar"
        tipoIncidenteReportado = "EMERGENCIA_MEDICA"
    },
    @{
        solicitante = @{
            nombreCompleto = "Carlos Rodríguez"
            telefono = "+59175345678"
            canalOrigen = "WHATSAPP"
        }
        ubicacion = @{
            descripcionTextual = "Mercado La Ramada"
            latitud = -17.7850
            longitud = -63.1810
        }
        descripcionOriginal = "Persona desmayada en el mercado, parece tener convulsiones"
        tipoIncidenteReportado = "EMERGENCIA_MEDICA"
    },
    @{
        solicitante = @{
            nombreCompleto = "Ana Martínez"
            telefono = "+59174456789"
            canalOrigen = "TELEGRAM"
        }
        ubicacion = @{
            descripcionTextual = "Terminal de Buses"
            latitud = -17.7900
            longitud = -63.1700
        }
        descripcionOriginal = "Niño de 5 años con quemaduras en el brazo por agua caliente"
        tipoIncidenteReportado = "EMERGENCIA_MEDICA"
    },
    @{
        solicitante = @{
            nombreCompleto = "Roberto Silva"
            telefono = "+59173567890"
            canalOrigen = "WHATSAPP"
        }
        ubicacion = @{
            descripcionTextual = "Av. Banzer y 3er Anillo"
            latitud = -17.7750
            longitud = -63.1900
        }
        descripcionOriginal = "Choque múltiple entre 3 vehículos, varios heridos con lesiones menores"
        tipoIncidenteReportado = "ACCIDENTE_TRAFICO"
    },
    @{
        solicitante = @{
            nombreCompleto = "Laura Gutiérrez"
            telefono = "+59172678901"
            canalOrigen = "TELEGRAM"
        }
        ubicacion = @{
            descripcionTextual = "Universidad Autónoma Gabriel René Moreno"
            latitud = -17.7840
            longitud = -63.1805
        }
        descripcionOriginal = "Estudiante con reacción alérgica severa, dificultad para respirar"
        tipoIncidenteReportado = "EMERGENCIA_MEDICA"
    },
    @{
        solicitante = @{
            nombreCompleto = "Pedro Sánchez"
            telefono = "+59171789012"
            canalOrigen = "WHATSAPP"
        }
        ubicacion = @{
            descripcionTextual = "Parque El Arenal"
            latitud = -17.7820
            longitud = -63.1825
        }
        descripcionOriginal = "Adulto mayor con caída, posible fractura de cadera, dolor intenso"
        tipoIncidenteReportado = "EMERGENCIA_MEDICA"
    },
    @{
        solicitante = @{
            nombreCompleto = "Sofía Ramírez"
            telefono = "+59170890123"
            canalOrigen = "TELEGRAM"
        }
        ubicacion = @{
            descripcionTextual = "Av. Roca y Coronado"
            latitud = -17.7880
            longitud = -63.1750
        }
        descripcionOriginal = "Mujer embarazada con contracciones fuertes y sangrado"
        tipoIncidenteReportado = "EMERGENCIA_MEDICA"
    },
    @{
        solicitante = @{
            nombreCompleto = "Diego Torres"
            telefono = "+59179901234"
            canalOrigen = "WHATSAPP"
        }
        ubicacion = @{
            descripcionTextual = "Estadio Ramón Tahuichi Aguilera"
            latitud = -17.7700
            longitud = -63.1950
        }
        descripcionOriginal = "Deportista con lesión grave en rodilla durante partido de fútbol"
        tipoIncidenteReportado = "EMERGENCIA_MEDICA"
    },
    @{
        solicitante = @{
            nombreCompleto = "Valentina Cruz"
            telefono = "+59178012345"
            canalOrigen = "TELEGRAM"
        }
        ubicacion = @{
            descripcionTextual = "Av. San Martín y 2do Anillo"
            latitud = -17.7790
            longitud = -63.1870
        }
        descripcionOriginal = "Accidente de motocicleta, conductor inconsciente, sangrado en la cara"
        tipoIncidenteReportado = "ACCIDENTE_TRAFICO"
    }
)

$exitosos = 0
$fallidos = 0
$incidentesCreados = @()

# Crear cada incidente
for ($i = 0; $i -lt $incidentes.Count; $i++) {
    $num = $i + 1
    Write-Host "[$num/10] Creando incidente: $($incidentes[$i].solicitante.nombreCompleto)..." -ForegroundColor Yellow

    try {
        $body = $incidentes[$i] | ConvertTo-Json -Depth 10
        $response = Invoke-RestMethod -Uri "$baseUrl/incidentes" -Method Post -Headers $headers -Body $body -ErrorAction Stop

        Write-Host "  ✓ Incidente creado exitosamente" -ForegroundColor Green
        Write-Host "    ID: $($response.id)" -ForegroundColor Gray
        Write-Host "    Estado: $($response.estado)" -ForegroundColor Gray
        Write-Host "    Prioridad: $($response.prioridad)" -ForegroundColor Gray

        $incidentesCreados += $response.id
        $exitosos++
    }
    catch {
        Write-Host "  ✗ Error al crear incidente" -ForegroundColor Red
        Write-Host "    $($_.Exception.Message)" -ForegroundColor Red
        $fallidos++
    }

    Write-Host ""
    Start-Sleep -Milliseconds 500
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Resumen de Pruebas" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total incidentes: 10" -ForegroundColor White
Write-Host "Exitosos: $exitosos" -ForegroundColor Green
Write-Host "Fallidos: $fallidos" -ForegroundColor Red
Write-Host ""

# Verificar que los incidentes se guardaron
if ($exitosos -gt 0) {
    Write-Host "Verificando incidentes creados..." -ForegroundColor Yellow
    Write-Host ""

    try {
        $url = $baseUrl + '/incidentes?page=0&size=20'
        $todosIncidentes = Invoke-RestMethod -Uri $url -Method Get -Headers $headers -ErrorAction Stop

        Write-Host "Total de incidentes en el sistema: $($todosIncidentes.totalElements)" -ForegroundColor Cyan
        Write-Host ""

        Write-Host "Últimos incidentes creados:" -ForegroundColor Cyan
        foreach ($inc in $todosIncidentes.content | Select-Object -First 10) {
            Write-Host "  • [$($inc.id.Substring(0,8))...] $($inc.descripcionOriginal.Substring(0, [Math]::Min(50, $inc.descripcionOriginal.Length)))..." -ForegroundColor White
            Write-Host "    Estado: $($inc.estado) | Prioridad: $($inc.prioridad) | Fecha: $($inc.fechaHoraRecepcion)" -ForegroundColor Gray
        }
    }
    catch {
        Write-Host "Error al verificar incidentes: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Pruebas completadas" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
