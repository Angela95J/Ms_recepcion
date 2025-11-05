@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set BASE_URL=http://localhost:8080/api
set API_KEY=admin-key-change-in-production-12345

echo ========================================
echo   Probando Microservicio de Recepción
echo ========================================
echo.

echo [1/10] Creando incidente 1: Accidente de tránsito...
curl -s -X POST "%BASE_URL%/incidentes" -H "Content-Type: application/json" -H "X-API-KEY: %API_KEY%" -d "{\"solicitante\":{\"nombreCompleto\":\"Juan Pérez García\",\"telefono\":\"+59177123456\",\"canalOrigen\":\"WHATSAPP\"},\"ubicacion\":{\"descripcionTextual\":\"Av. Cristo Redentor y 4to Anillo\",\"latitud\":-17.783,\"longitud\":-63.182},\"descripcionOriginal\":\"Accidente de tránsito grave, persona con sangrado\",\"tipoIncidenteReportado\":\"ACCIDENTE_TRAFICO\"}" > nul
echo OK

echo [2/10] Creando incidente 2: Emergencia cardíaca...
curl -s -X POST "%BASE_URL%/incidentes" -H "Content-Type: application/json" -H "X-API-KEY: %API_KEY%" -d "{\"solicitante\":{\"nombreCompleto\":\"María López\",\"telefono\":\"+59176234567\",\"canalOrigen\":\"TELEGRAM\"},\"ubicacion\":{\"descripcionTextual\":\"Plaza 24 de Septiembre\",\"latitud\":-17.7833,\"longitud\":-63.1821},\"descripcionOriginal\":\"Emergencia cardíaca, dolor intenso en el pecho\",\"tipoIncidenteReportado\":\"EMERGENCIA_MEDICA\"}" > nul
echo OK

echo [3/10] Creando incidente 3: Persona desmayada...
curl -s -X POST "%BASE_URL%/incidentes" -H "Content-Type: application/json" -H "X-API-KEY: %API_KEY%" -d "{\"solicitante\":{\"nombreCompleto\":\"Carlos Rodríguez\",\"telefono\":\"+59175345678\",\"canalOrigen\":\"WHATSAPP\"},\"ubicacion\":{\"descripcionTextual\":\"Mercado La Ramada\",\"latitud\":-17.7850,\"longitud\":-63.1810},\"descripcionOriginal\":\"Persona desmayada con convulsiones\",\"tipoIncidenteReportado\":\"EMERGENCIA_MEDICA\"}" > nul
echo OK

echo [4/10] Creando incidente 4: Niño con quemaduras...
curl -s -X POST "%BASE_URL%/incidentes" -H "Content-Type: application/json" -H "X-API-KEY: %API_KEY%" -d "{\"solicitante\":{\"nombreCompleto\":\"Ana Martínez\",\"telefono\":\"+59174456789\",\"canalOrigen\":\"TELEGRAM\"},\"ubicacion\":{\"descripcionTextual\":\"Terminal de Buses\",\"latitud\":-17.7900,\"longitud\":-63.1700},\"descripcionOriginal\":\"Niño de 5 años con quemaduras en el brazo\",\"tipoIncidenteReportado\":\"EMERGENCIA_MEDICA\"}" > nul
echo OK

echo [5/10] Creando incidente 5: Choque múltiple...
curl -s -X POST "%BASE_URL%/incidentes" -H "Content-Type: application/json" -H "X-API-KEY: %API_KEY%" -d "{\"solicitante\":{\"nombreCompleto\":\"Roberto Silva\",\"telefono\":\"+59173567890\",\"canalOrigen\":\"WHATSAPP\"},\"ubicacion\":{\"descripcionTextual\":\"Av. Banzer y 3er Anillo\",\"latitud\":-17.7750,\"longitud\":-63.1900},\"descripcionOriginal\":\"Choque múltiple entre 3 vehículos\",\"tipoIncidenteReportado\":\"ACCIDENTE_TRAFICO\"}" > nul
echo OK

echo [6/10] Creando incidente 6: Reacción alérgica...
curl -s -X POST "%BASE_URL%/incidentes" -H "Content-Type: application/json" -H "X-API-KEY: %API_KEY%" -d "{\"solicitante\":{\"nombreCompleto\":\"Laura Gutiérrez\",\"telefono\":\"+59172678901\",\"canalOrigen\":\"TELEGRAM\"},\"ubicacion\":{\"descripcionTextual\":\"Universidad UAGRM\",\"latitud\":-17.7840,\"longitud\":-63.1805},\"descripcionOriginal\":\"Estudiante con reacción alérgica severa\",\"tipoIncidenteReportado\":\"EMERGENCIA_MEDICA\"}" > nul
echo OK

echo [7/10] Creando incidente 7: Adulto mayor caída...
curl -s -X POST "%BASE_URL%/incidentes" -H "Content-Type: application/json" -H "X-API-KEY: %API_KEY%" -d "{\"solicitante\":{\"nombreCompleto\":\"Pedro Sánchez\",\"telefono\":\"+59171789012\",\"canalOrigen\":\"WHATSAPP\"},\"ubicacion\":{\"descripcionTextual\":\"Parque El Arenal\",\"latitud\":-17.7820,\"longitud\":-63.1825},\"descripcionOriginal\":\"Adulto mayor con caída, posible fractura de cadera\",\"tipoIncidenteReportado\":\"EMERGENCIA_MEDICA\"}" > nul
echo OK

echo [8/10] Creando incidente 8: Mujer embarazada...
curl -s -X POST "%BASE_URL%/incidentes" -H "Content-Type: application/json" -H "X-API-KEY: %API_KEY%" -d "{\"solicitante\":{\"nombreCompleto\":\"Sofía Ramírez\",\"telefono\":\"+59170890123\",\"canalOrigen\":\"TELEGRAM\"},\"ubicacion\":{\"descripcionTextual\":\"Av. Roca y Coronado\",\"latitud\":-17.7880,\"longitud\":-63.1750},\"descripcionOriginal\":\"Mujer embarazada con contracciones fuertes\",\"tipoIncidenteReportado\":\"EMERGENCIA_MEDICA\"}" > nul
echo OK

echo [9/10] Creando incidente 9: Lesión deportiva...
curl -s -X POST "%BASE_URL%/incidentes" -H "Content-Type: application/json" -H "X-API-KEY: %API_KEY%" -d "{\"solicitante\":{\"nombreCompleto\":\"Diego Torres\",\"telefono\":\"+59179901234\",\"canalOrigen\":\"WHATSAPP\"},\"ubicacion\":{\"descripcionTextual\":\"Estadio Ramón Tahuichi\",\"latitud\":-17.7700,\"longitud\":-63.1950},\"descripcionOriginal\":\"Deportista con lesión grave en rodilla\",\"tipoIncidenteReportado\":\"EMERGENCIA_MEDICA\"}" > nul
echo OK

echo [10/10] Creando incidente 10: Accidente de moto...
curl -s -X POST "%BASE_URL%/incidentes" -H "Content-Type: application/json" -H "X-API-KEY: %API_KEY%" -d "{\"solicitante\":{\"nombreCompleto\":\"Valentina Cruz\",\"telefono\":\"+59178012345\",\"canalOrigen\":\"TELEGRAM\"},\"ubicacion\":{\"descripcionTextual\":\"Av. San Martín y 2do Anillo\",\"latitud\":-17.7790,\"longitud\":-63.1870},\"descripcionOriginal\":\"Accidente de motocicleta, conductor inconsciente\",\"tipoIncidenteReportado\":\"ACCIDENTE_TRAFICO\"}" > nul
echo OK

echo.
echo ========================================
echo   Verificando incidentes creados
echo ========================================
echo.

curl -s -X GET "%BASE_URL%/incidentes?page=0&size=10" -H "X-API-KEY: %API_KEY%"

echo.
echo.
echo ========================================
echo   Pruebas completadas
echo ========================================

pause
