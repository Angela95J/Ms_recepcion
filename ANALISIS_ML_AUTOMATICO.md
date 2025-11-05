# 🤖 Análisis ML Automático - Documentación de Implementación

## 📋 Resumen

Se ha implementado la **integración automática de análisis de Machine Learning** en el microservicio de recepción de incidentes. Ahora, cuando un solicitante envía un incidente con descripción e imágenes, el sistema automáticamente:

1. ✅ Analiza el texto de la descripción usando ML (puerto 8001)
2. ✅ Analiza las imágenes subidas usando ML (puerto 8002)
3. ✅ Calcula la prioridad final combinando ambos análisis (60% texto + 40% imagen)
4. ✅ Actualiza el estado del incidente automáticamente

---

## 🏗️ Arquitectura Implementada

```
┌─────────────────────────────────────────────────────────┐
│                    Usuario/Bot (n8n)                    │
└───────────────────────┬─────────────────────────────────┘
                        │
                        │ POST /api/incidentes
                        ▼
┌─────────────────────────────────────────────────────────┐
│           IncidenteController (Java)                    │
│                                                          │
│  POST /api/incidentes                                   │
│    ├─ Crear incidente (solicitante + ubicación)        │
│    └─ Retornar respuesta inmediata                     │
└───────────────────────┬─────────────────────────────────┘
                        │
                        │ Llamada automática
                        ▼
┌─────────────────────────────────────────────────────────┐
│         IncidenteServiceImpl (Java)                     │
│                                                          │
│  1. Guardar incidente en BD (estado: RECIBIDO)         │
│  2. Llamar analisisMlOrchestrationService               │
└───────────────────────┬─────────────────────────────────┘
                        │
                        │ @Async
                        ▼
┌─────────────────────────────────────────────────────────┐
│    AnalisisMlOrchestrationService (Orquestador)        │
│                                                          │
│  analizarTextoAutomaticamente()                         │
│    1. Cambiar estado → EN_ANALISIS_TEXTO               │
│    2. Llamar MlTextoClient (puerto 8001)               │
│    3. Guardar resultado en analisis_ml_texto           │
│    4. Actualizar incidente con prioridad               │
│    5. Cambiar estado → ANALIZADO                        │
│    6. Calcular prioridad final                          │
└───────────────────────┬─────────────────────────────────┘
                        │
        ┌───────────────┴───────────────┐
        │                               │
        ▼                               ▼
┌──────────────────┐          ┌──────────────────┐
│  MlTextoClient   │          │ MlImagenClient   │
│  (Puerto 8001)   │          │  (Puerto 8002)   │
│                  │          │                  │
│  - K-means       │          │  - K-means       │
│  - TF-IDF        │          │  - OpenCV        │
│  - NLTK          │          │  - Detección     │
└──────────────────┘          └──────────────────┘


┌─────────────────────────────────────────────────────────┐
│                 Subida de Imagen                        │
└───────────────────────┬─────────────────────────────────┘
                        │
                        │ POST /api/multimedia/incidente/{id}/subir
                        ▼
┌─────────────────────────────────────────────────────────┐
│         MultimediaController (Java)                     │
│                                                          │
│  POST /multimedia/incidente/{id}/subir                  │
│    ├─ Validar y guardar archivo en disco               │
│    ├─ Crear registro en BD                             │
│    └─ Retornar respuesta inmediata                     │
└───────────────────────┬─────────────────────────────────┘
                        │
                        │ Llamada automática
                        ▼
┌─────────────────────────────────────────────────────────┐
│         MultimediaServiceImpl (Java)                    │
│                                                          │
│  1. Guardar archivo físico en ./uploads/               │
│  2. Guardar registro en multimedia (BD)                │
│  3. Llamar analisisMlOrchestrationService               │
└───────────────────────┬─────────────────────────────────┘
                        │
                        │ @Async
                        ▼
┌─────────────────────────────────────────────────────────┐
│    AnalisisMlOrchestrationService (Orquestador)        │
│                                                          │
│  analizarImagenAutomaticamente()                        │
│    1. Cambiar estado → EN_ANALISIS_IMAGEN              │
│    2. Llamar MlImagenClient (puerto 8002)              │
│    3. Guardar resultado en analisis_ml_imagen          │
│    4. Actualizar incidente con severidad/veracidad     │
│    5. Cambiar estado → ANALIZADO                        │
│    6. Calcular prioridad final                          │
└─────────────────────────────────────────────────────────┘
```

---

## 🆕 Nuevos Componentes Creados

### 1. `AnalisisMlOrchestrationService` (Interface)

**Ubicación:** `com.recepcion.recepcion.service.AnalisisMlOrchestrationService`

**Propósito:** Define los contratos para orquestar el análisis ML automático

**Métodos:**
- `analizarTextoAutomaticamente(UUID incidenteId)` - Analiza texto del incidente
- `analizarImagenAutomaticamente(UUID multimediaId)` - Analiza imagen subida
- `analizarIncidenteCompleto(UUID incidenteId)` - Analiza texto + todas las imágenes

---

### 2. `AnalisisMlOrchestrationServiceImpl` (Implementación)

**Ubicación:** `com.recepcion.recepcion.service.impl.AnalisisMlOrchestrationServiceImpl`

**Características:**
- ✅ **Procesamiento asíncrono** con `@Async` para no bloquear la respuesta al usuario
- ✅ **Manejo de errores robusto** - No falla la creación del incidente si ML falla
- ✅ **Health checks** - Verifica disponibilidad de servicios ML antes de llamarlos
- ✅ **Actualización de estados** - Mantiene el estado del incidente actualizado
- ✅ **Cálculo de prioridad final** - Combina prioridades de texto (60%) e imagen (40%)

**Dependencias inyectadas:**
```java
private final IncidenteRepository incidenteRepository;
private final MultimediaRepository multimediaRepository;
private final AnalisisMlTextoRepository analisisTextoRepository;
private final AnalisisMlImagenRepository analisisImagenRepository;
private final MlTextoClient mlTextoClient;
private final MlImagenClient mlImagenClient;
```

**Configuración:**
```yaml
ml:
  texto:
    enabled: true  # Habilitar/deshabilitar análisis de texto
  imagen:
    enabled: true  # Habilitar/deshabilitar análisis de imagen
```

---

## 🔄 Flujo Completo de Ejecución

### **Escenario 1: Usuario crea incidente con descripción**

```
1. Usuario → POST /api/incidentes
   {
     "solicitante": {...},
     "ubicacion": {...},
     "descripcionOriginal": "Accidente grave..."
   }

2. IncidenteController recibe request
   └─ IncidenteServiceImpl.crear()
      └─ Guarda incidente (estado: RECIBIDO)
      └─ Retorna respuesta inmediata al usuario ✅

3. [ASÍNCRONO] analisisMlOrchestrationService.analizarTextoAutomaticamente()
   └─ Cambia estado → EN_ANALISIS_TEXTO
   └─ Llama mlTextoClient → POST http://localhost:8001/api/ml/analizar-texto
   └─ Servicio Python/FastAPI responde con:
      {
        "prioridad_calculada": 1,
        "nivel_gravedad": 5,
        "tipo_incidente_predicho": "Crítico",
        "score_confianza": 0.92,
        "palabras_clave_criticas": ["accidente", "grave", "inconsciente"]
      }
   └─ Guarda en analisis_ml_texto
   └─ Actualiza incidente.prioridadTexto = 1
   └─ Actualiza incidente.tipoIncidenteClasificado = "Crítico"
   └─ Cambia estado → ANALIZADO
   └─ Calcula prioridad final (si hay imagen también)
```

---

### **Escenario 2: Usuario sube imagen**

```
1. Usuario → POST /api/multimedia/incidente/{id}/subir
   FormData: archivo=accidente.jpg

2. MultimediaController recibe request
   └─ MultimediaServiceImpl.subirArchivo()
      └─ Valida archivo (tipo, tamaño)
      └─ Guarda en ./uploads/UUID.jpg
      └─ Guarda registro en BD (requiereAnalisisMl=true)
      └─ Retorna respuesta inmediata al usuario ✅

3. [ASÍNCRONO] analisisMlOrchestrationService.analizarImagenAutomaticamente()
   └─ Cambia estado incidente → EN_ANALISIS_IMAGEN
   └─ Llama mlImagenClient → POST http://localhost:8002/api/ml/analizar-imagen
      {
        "imagenPath": "./uploads/UUID.jpg",
        "multimediaId": "...",
        "incidenteId": "..."
      }
   └─ Servicio Python/FastAPI responde con:
      {
        "es_imagen_accidente": true,
        "nivel_gravedad_visual": 4,
        "score_veracidad": 0.88,
        "objetos_detectados": ["vehículo", "persona"],
        "score_confianza": 0.85
      }
   └─ Guarda en analisis_ml_imagen
   └─ Actualiza multimedia.analisisCompletado = true
   └─ Actualiza incidente.prioridadImagen = 4
   └─ Actualiza incidente.scoreVeracidad = 0.88
   └─ Cambia estado → ANALIZADO
   └─ Calcula prioridad final:
      prioridad_final = (prioridadTexto * 0.6) + (prioridadImagen * 0.4)
      prioridad_final = (1 * 0.6) + (4 * 0.4) = 0.6 + 1.6 = 2.2 ≈ 2
```

---

## 📊 Cálculo de Prioridad Final

La prioridad final se calcula combinando los análisis:

```java
private void calcularPrioridadFinal(Incidente incidente) {
    Integer prioridadTexto = incidente.getPrioridadTexto();
    Integer prioridadImagen = incidente.getPrioridadImagen();

    if (prioridadTexto != null && prioridadImagen != null) {
        // Combinar ambas (60% texto + 40% imagen)
        int prioridadFinal = (int) Math.round((prioridadTexto * 0.6 + prioridadImagen * 0.4));
        incidente.setPrioridadFinal(prioridadFinal);
    } else if (prioridadTexto != null) {
        // Solo hay análisis de texto
        incidente.setPrioridadFinal(prioridadTexto);
    } else if (prioridadImagen != null) {
        // Solo hay análisis de imagen
        incidente.setPrioridadFinal(prioridadImagen);
    }
}
```

**Ejemplo:**
- Prioridad texto: 1 (Crítico)
- Prioridad imagen: 4 (Moderado)
- Prioridad final: (1 × 0.6) + (4 × 0.4) = 0.6 + 1.6 = **2.2 ≈ 2** (Urgente)

---

## 🔧 Modificaciones Realizadas

### 1. **IncidenteServiceImpl.java**

**Cambios:**
```java
// Agregada dependencia
private final AnalisisMlOrchestrationService analisisMlOrchestrationService;

// En método crear()
incidente = incidenteRepository.save(incidente);

// NUEVO: Análisis automático
try {
    analisisMlOrchestrationService.analizarTextoAutomaticamente(incidente.getId());
} catch (Exception e) {
    log.error("Error al iniciar análisis ML automático: {}", e.getMessage());
    // No lanzar excepción para no bloquear la creación
}

return incidenteMapper.toResponse(incidente);
```

---

### 2. **MultimediaServiceImpl.java**

**Cambios:**
```java
// Agregada dependencia
private final AnalisisMlOrchestrationService analisisMlOrchestrationService;

// En método subirArchivo()
multimedia = multimediaRepository.save(multimedia);

// NUEVO: Análisis automático de imagen
if (multimedia.getTipoArchivo() == TipoArchivo.IMAGEN) {
    try {
        analisisMlOrchestrationService.analizarImagenAutomaticamente(multimedia.getId());
    } catch (Exception e) {
        log.error("Error al iniciar análisis ML de imagen automático: {}", e.getMessage());
        // No lanzar excepción para no bloquear la subida
    }
}

return multimediaMapper.toResponse(multimedia);
```

---

### 3. **RecepcionApplication.java**

**Cambios:**
```java
@SpringBootApplication
@EnableAsync  // ← NUEVO: Habilita procesamiento asíncrono
public class RecepcionApplication {
    // ...
}
```

---

## 🧪 Pruebas

### Script de Prueba Completo

Se han creado dos scripts de prueba end-to-end:

#### **PowerShell (Windows)**
```powershell
.\test_flujo_completo.ps1
```

#### **Bash (Linux/Mac)**
```bash
./test_flujo_completo.sh
```

**Lo que prueban:**
1. ✅ Crear incidente con solicitante + ubicación + descripción
2. ✅ Verificar análisis de texto automático (espera 3 segundos)
3. ✅ Subir imagen al incidente
4. ✅ Verificar análisis de imagen automático (espera 5 segundos)
5. ✅ Verificar prioridad final calculada
6. ✅ Consultar análisis detallado de texto
7. ✅ Consultar análisis detallado de imagen

---

### Prueba Manual con cURL

```bash
# 1. Crear incidente
curl -X POST http://localhost:8080/api/incidentes \
  -H "Content-Type: application/json" \
  -d '{
    "solicitante": {
      "nombreCompleto": "Juan Pérez",
      "telefono": "+58412123456",
      "canalOrigen": "WHATSAPP"
    },
    "ubicacion": {
      "latitud": 10.4806,
      "longitud": -66.9036,
      "descripcionTextual": "Av. Principal"
    },
    "descripcionOriginal": "Accidente grave con heridos",
    "tipoIncidenteReportado": "ACCIDENTE_TRAFICO"
  }'

# Respuesta: {"id": "abc-123-def", ...}

# 2. Esperar 3 segundos

# 3. Verificar análisis de texto
curl http://localhost:8080/api/incidentes/abc-123-def/detalle

# 4. Subir imagen
curl -X POST http://localhost:8080/api/multimedia/incidente/abc-123-def/subir \
  -F "archivo=@./foto_accidente.jpg" \
  -F "descripcion=Foto del accidente" \
  -F "esPrincipal=true"

# 5. Esperar 5 segundos

# 6. Verificar análisis completo
curl http://localhost:8080/api/incidentes/abc-123-def/detalle
```

---

## 📝 Estados del Incidente

El incidente pasa por los siguientes estados:

```
RECIBIDO
   ↓
EN_ANALISIS_TEXTO (durante análisis)
   ↓
ANALIZADO (texto completado)
   ↓
EN_ANALISIS_IMAGEN (cuando se sube imagen)
   ↓
ANALIZADO (análisis completo)
   ↓
APROBADO / RECHAZADO (acción manual o automática)
```

---

## ⚙️ Configuración

### `application.yml`

```yaml
# Configuración de servicios ML
ml:
  texto:
    base-url: http://localhost:8001
    enabled: true  # false para deshabilitar
  imagen:
    base-url: http://localhost:8002
    enabled: true  # false para deshabilitar

# Configuración de multimedia
app:
  multimedia:
    upload-dir: ./uploads
    max-file-size: 10485760  # 10MB
```

---

## 🚀 Ventajas de la Implementación

1. ✅ **No bloqueante**: El usuario recibe respuesta inmediata, el análisis se hace en background
2. ✅ **Tolerante a fallos**: Si el servicio ML falla, no se bloquea la creación del incidente
3. ✅ **Escalable**: Procesamiento asíncrono permite manejar múltiples análisis en paralelo
4. ✅ **Configurable**: Se puede habilitar/deshabilitar análisis de texto e imagen
5. ✅ **Auditable**: Todos los análisis se guardan en BD con timestamps
6. ✅ **Health checks**: Verifica disponibilidad de servicios antes de llamarlos
7. ✅ **Trazabilidad**: Logs detallados de cada paso del proceso

---

## 🔍 Endpoints Relevantes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/incidentes` | Crear incidente (análisis de texto automático) |
| POST | `/api/multimedia/incidente/{id}/subir` | Subir imagen (análisis de imagen automático) |
| GET | `/api/incidentes/{id}/detalle` | Ver incidente con análisis completo |
| GET | `/api/analisis-texto/incidente/{id}` | Ver análisis de texto detallado |
| GET | `/api/analisis-imagen/incidente/{id}` | Ver análisis de imagen detallado |
| POST | `/api/incidentes/{id}/analisis-texto` | Forzar análisis de texto manual |
| POST | `/api/incidentes/{id}/analisis-imagen` | Forzar análisis de imagen manual |
| POST | `/api/incidentes/{id}/calcular-prioridad` | Recalcular prioridad final |

---

## 🛠️ Troubleshooting

### Problema: El análisis no se ejecuta automáticamente

**Causas posibles:**
1. Servicios ML no están corriendo (puertos 8001, 8002)
2. `@EnableAsync` no está habilitado en `RecepcionApplication`
3. Configuración `ml.texto.enabled` o `ml.imagen.enabled` es `false`

**Solución:**
```bash
# Verificar servicios ML
curl http://localhost:8001/api/ml/salud
curl http://localhost:8002/api/ml/salud

# Revisar configuración en application.yml
ml:
  texto:
    enabled: true
  imagen:
    enabled: true
```

---

### Problema: El análisis tarda mucho

**Causas:**
- Modelos ML no están entrenados o son muy lentos
- Servicios ML sobrecargados

**Solución:**
- Revisar logs de servicios Python
- Verificar que los modelos están cargados correctamente
- Considerar usar cache o optimizar modelos

---

## 📚 Referencias

- [Spring @Async Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling-annotation-support-async)
- [RestTemplate Documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)
- README_ML_INTEGRATION.md - Arquitectura ML completa
- QUICK_START.md - Guía de inicio rápido

---

**🎉 ¡Implementación completa y lista para usar!**
