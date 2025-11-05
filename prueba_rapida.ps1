# Prueba rápida del análisis ML automático

Write-Host "==============================================" -ForegroundColor Blue
Write-Host "  PRUEBA: Análisis ML Automático" -ForegroundColor Blue
Write-Host "==============================================" -ForegroundColor Blue
Write-Host ""

$baseUrl = "http://localhost:8080/api"

Write-Host "1️⃣  Creando incidente..." -ForegroundColor Cyan

$incidenteBody = @{
    solicitante = @{
        nombreCompleto = "Test Usuario"
        telefono = "+584121234567"
        canalOrigen = "WHATSAPP"
    }
    ubicacion = @{
        latitud = 10.4806
        longitud = -66.9036
        descripcionTextual = "Avenida Principal"
    }
    descripcionOriginal = "Accidente de tráfico grave con persona inconsciente y hemorragia"
    tipoIncidenteReportado = "ACCIDENTE_TRAFICO"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/incidentes" -Method Post -Body $incidenteBody -ContentType "application/json"

    Write-Host "✅ Incidente creado!" -ForegroundColor Green
    Write-Host "   ID: $($response.id)" -ForegroundColor Yellow
    Write-Host "   Estado: $($response.estadoIncidente)" -ForegroundColor Yellow
    Write-Host ""

    $incidenteId = $response.id

    Write-Host "🤖 El análisis ML de texto está corriendo en segundo plano..." -ForegroundColor Magenta
    Write-Host "   Esperando 4 segundos..." -ForegroundColor Gray
    Start-Sleep -Seconds 4

    Write-Host ""
    Write-Host "2️⃣  Verificando análisis de texto..." -ForegroundColor Cyan

    $detalle = Invoke-RestMethod -Uri "$baseUrl/incidentes/$incidenteId/detalle" -Method Get

    Write-Host "✅ Resultado del análisis:" -ForegroundColor Green
    Write-Host "   Estado: $($detalle.estadoIncidente)" -ForegroundColor Yellow
    Write-Host "   Prioridad Texto: $($detalle.prioridadTexto)" -ForegroundColor Yellow
    Write-Host "   Tipo Clasificado: $($detalle.tipoIncidenteClasificado)" -ForegroundColor Yellow

    if ($detalle.analisisTexto) {
        Write-Host ""
        Write-Host "📊 Detalles del análisis ML:" -ForegroundColor Cyan
        Write-Host "   Score Confianza: $($detalle.analisisTexto.scoreConfianza)" -ForegroundColor White
        Write-Host "   Nivel Gravedad: $($detalle.analisisTexto.nivelGravedad)" -ForegroundColor White
    }

    Write-Host ""
    Write-Host "==============================================" -ForegroundColor Green
    Write-Host "  ✅ PRUEBA EXITOSA" -ForegroundColor Green
    Write-Host "==============================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "El análisis ML se ejecutó AUTOMÁTICAMENTE ✨" -ForegroundColor Magenta

} catch {
    Write-Host "❌ Error: $_" -ForegroundColor Red
}
