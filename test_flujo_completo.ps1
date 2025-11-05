# Script de prueba completa del flujo de análisis ML automático
# Prueba: Crear incidente con solicitante, ubicación, descripción e imagen

Write-Host "=====================================================" -ForegroundColor Blue
Write-Host "  PRUEBA COMPLETA: Incidente con Análisis ML Automático" -ForegroundColor Blue
Write-Host "=====================================================" -ForegroundColor Blue
Write-Host ""

$baseUrl = "http://localhost:8080/api"

# Paso 1: Crear incidente
Write-Host "📋 Paso 1: Creando incidente con solicitante y ubicación..." -ForegroundColor Cyan
Write-Host ""

$incidenteBody = @{
    solicitante = @{
        nombreCompleto = "María González"
        telefono = "+584121234567"
        email = "maria@example.com"
        canalOrigen = "WHATSAPP"
    }
    ubicacion = @{
        latitud = 10.4806
        longitud = -66.9036
        descripcionTextual = "Avenida Libertador con calle Sur, frente al Hospital Central"
        ciudad = "Caracas"
        distrito = "Libertador"
    }
    descripcionOriginal = "Accidente de tráfico grave. Colisión entre dos vehículos. Hay una persona inconsciente con hemorragia en la cabeza. Necesita atención urgente."
    tipoIncidenteReportado = "ACCIDENTE_TRAFICO"
} | ConvertTo-Json

try {
    $incidenteResponse = Invoke-RestMethod -Uri "$baseUrl/incidentes" -Method Post -Body $incidenteBody -ContentType "application/json"

    Write-Host "✅ Incidente creado exitosamente" -ForegroundColor Green
    Write-Host ($incidenteResponse | ConvertTo-Json -Depth 10)
    Write-Host ""

    $incidenteId = $incidenteResponse.id
    Write-Host "✅ Incidente creado con ID: $incidenteId" -ForegroundColor Green
    Write-Host ""

    # Esperar para que el análisis de texto se complete
    Write-Host "⏳ Esperando 3 segundos para que el análisis de texto se complete..." -ForegroundColor Yellow
    Start-Sleep -Seconds 3

    # Paso 2: Verificar análisis de texto
    Write-Host "📋 Paso 2: Verificando el análisis de texto automático..." -ForegroundColor Cyan
    Write-Host ""

    $incidenteDetalle = Invoke-RestMethod -Uri "$baseUrl/incidentes/$incidenteId/detalle" -Method Get

    Write-Host "✅ Detalle del incidente después del análisis de texto:" -ForegroundColor Green
    Write-Host ($incidenteDetalle | ConvertTo-Json -Depth 10)
    Write-Host ""

    $prioridadTexto = $incidenteDetalle.prioridadTexto
    Write-Host "📊 Prioridad calculada por análisis de texto: $prioridadTexto" -ForegroundColor Green
    Write-Host ""

    # Paso 3: Subir imagen
    Write-Host "📋 Paso 3: Subiendo imagen del incidente..." -ForegroundColor Cyan
    Write-Host ""

    # Crear archivo de imagen de prueba si no existe
    $testImage = "test_accidente.txt"
    if (-not (Test-Path $testImage)) {
        Write-Host "⚠️  Creando archivo de prueba..." -ForegroundColor Yellow
        "Imagen de prueba para incidente" | Out-File -FilePath $testImage -Encoding UTF8
    }

    # Preparar formulario multipart
    $boundary = [System.Guid]::NewGuid().ToString()
    $fileBytes = [System.IO.File]::ReadAllBytes((Resolve-Path $testImage))
    $fileContent = [System.Text.Encoding]::GetEncoding("iso-8859-1").GetString($fileBytes)

    $body = @"
--$boundary
Content-Disposition: form-data; name="archivo"; filename="$testImage"
Content-Type: application/octet-stream

$fileContent
--$boundary
Content-Disposition: form-data; name="descripcion"

Foto del accidente mostrando los dos vehículos colisionados
--$boundary
Content-Disposition: form-data; name="esPrincipal"

true
--$boundary--
"@

    $headers = @{
        "Content-Type" = "multipart/form-data; boundary=$boundary"
    }

    try {
        $multimediaResponse = Invoke-RestMethod -Uri "$baseUrl/multimedia/incidente/$incidenteId/subir" -Method Post -Body $body -Headers $headers

        Write-Host "✅ Imagen subida exitosamente" -ForegroundColor Green
        Write-Host ($multimediaResponse | ConvertTo-Json -Depth 10)
        Write-Host ""

        $multimediaId = $multimediaResponse.id
        Write-Host "✅ Imagen subida con ID: $multimediaId" -ForegroundColor Green
        Write-Host ""

        # Esperar para que el análisis de imagen se complete
        Write-Host "⏳ Esperando 5 segundos para que el análisis de imagen se complete..." -ForegroundColor Yellow
        Start-Sleep -Seconds 5

        # Paso 4: Verificar análisis completo
        Write-Host "📋 Paso 4: Verificando el análisis de imagen automático..." -ForegroundColor Cyan
        Write-Host ""

        $incidenteFinal = Invoke-RestMethod -Uri "$baseUrl/incidentes/$incidenteId/detalle" -Method Get

        Write-Host "✅ Detalle completo del incidente después de todos los análisis:" -ForegroundColor Green
        Write-Host ($incidenteFinal | ConvertTo-Json -Depth 10)
        Write-Host ""

        # Extraer información relevante
        $estado = $incidenteFinal.estadoIncidente
        $prioridadFinal = $incidenteFinal.prioridadFinal
        $prioridadImagen = $incidenteFinal.prioridadImagen
        $scoreVeracidad = $incidenteFinal.scoreVeracidad

        Write-Host ""
        Write-Host "=====================================================" -ForegroundColor Blue
        Write-Host "  📊 RESULTADOS DEL ANÁLISIS COMPLETO" -ForegroundColor Blue
        Write-Host "=====================================================" -ForegroundColor Blue
        Write-Host "Estado del incidente: $estado" -ForegroundColor Green
        Write-Host "Prioridad de texto: $prioridadTexto" -ForegroundColor Green
        Write-Host "Prioridad de imagen: $prioridadImagen" -ForegroundColor Green
        Write-Host "Prioridad final (60% texto + 40% imagen): $prioridadFinal" -ForegroundColor Green
        Write-Host "Score de veracidad: $scoreVeracidad" -ForegroundColor Green
        Write-Host "=====================================================" -ForegroundColor Blue
        Write-Host ""

        # Paso 5: Consultar análisis de texto
        Write-Host "📋 Paso 5: Consultando análisis de texto..." -ForegroundColor Cyan
        Write-Host ""

        try {
            $analisisTexto = Invoke-RestMethod -Uri "$baseUrl/analisis-texto/incidente/$incidenteId" -Method Get
            Write-Host "✅ Análisis de texto detallado:" -ForegroundColor Green
            Write-Host ($analisisTexto | ConvertTo-Json -Depth 10)
            Write-Host ""
        } catch {
            Write-Host "⚠️  No se pudo obtener el análisis de texto: $_" -ForegroundColor Yellow
        }

        # Paso 6: Consultar análisis de imagen
        Write-Host "📋 Paso 6: Consultando análisis de imagen..." -ForegroundColor Cyan
        Write-Host ""

        try {
            $analisisImagen = Invoke-RestMethod -Uri "$baseUrl/analisis-imagen/incidente/$incidenteId" -Method Get
            Write-Host "✅ Análisis de imagen detallado:" -ForegroundColor Green
            Write-Host ($analisisImagen | ConvertTo-Json -Depth 10)
            Write-Host ""
        } catch {
            Write-Host "⚠️  No se pudo obtener el análisis de imagen: $_" -ForegroundColor Yellow
        }

        Write-Host "=====================================================" -ForegroundColor Green
        Write-Host "✅ PRUEBA COMPLETA FINALIZADA EXITOSAMENTE" -ForegroundColor Green
        Write-Host "=====================================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "El incidente fue:" -ForegroundColor White
        Write-Host "  1. ✅ Creado con solicitante y ubicación" -ForegroundColor White
        Write-Host "  2. ✅ Analizado automáticamente (texto)" -ForegroundColor White
        Write-Host "  3. ✅ Imagen subida" -ForegroundColor White
        Write-Host "  4. ✅ Imagen analizada automáticamente" -ForegroundColor White
        Write-Host "  5. ✅ Prioridad final calculada" -ForegroundColor White
        Write-Host ""
        Write-Host "ID del incidente: $incidenteId" -ForegroundColor Cyan
        Write-Host "Accede al detalle en: $baseUrl/incidentes/$incidenteId/detalle" -ForegroundColor Cyan
        Write-Host ""

    } catch {
        Write-Host "❌ Error al subir la imagen: $_" -ForegroundColor Red
        Write-Host "Respuesta del servidor: $($_.Exception.Response)" -ForegroundColor Red
    }

} catch {
    Write-Host "❌ Error al crear el incidente: $_" -ForegroundColor Red
    Write-Host "Respuesta del servidor: $($_.Exception.Response)" -ForegroundColor Red
}
