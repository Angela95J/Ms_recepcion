# 🧪 Instrucciones para Probar el Análisis ML Automático

## ✅ Estado Actual del Sistema

**Todo está implementado y funcionando correctamente:**
- ✅ Microservicio Java compilado sin errores
- ✅ Dependencia circular resuelta con `@Lazy`
- ✅ Análisis ML automático implementado
- ✅ Procesamiento asíncrono habilitado con `@EnableAsync`

---

## 🚀 Pasos para Probar el Sistema

### **Paso 1: Iniciar los Servicios**

#### **Terminal 1: Servicio ML de Texto (Puerto 8001)**
```bash
cd ml_analisis_texto
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
python train_model.py
python -m uvicorn app.main:app --reload --port 8001
```

**Verificar:** Abrir http://localhost:8001/docs en el navegador

---

#### **Terminal 2: Servicio ML de Imagen (Puerto 8002)**
```bash
cd ml_analisis_imagen
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
python train_model.py
python -m uvicorn app.main:app --reload --port 8002
```

**Verificar:** Abrir http://localhost:8002/docs en el navegador

---

#### **Terminal 3: Microservicio Java (Puerto 8080)**
```bash
cd recepcion
./mvnw spring-boot:run
```

**Esperar hasta ver:**
```
Started RecepcionApplication in X.XXX seconds
Tomcat started on port 8080 (http) with context path '/api'
```

---

### **Paso 2: Probar Creación de Incidente con Análisis Automático**

#### **Opción A: Postman / Insomnia**

**1. Crear Incidente**

```
POST http://localhost:8080/api/incidentes
Content-Type: application/json

{
  "solicitante": {
    "nombreCompleto": "María González",
    "telefono": "+584121234567",
    "email": "maria@example.com",
    "canalOrigen": "WHATSAPP"
  },
  "ubicacion": {
    "latitud": 10.4806,
    "longitud": -66.9036,
    "descripcionTextual": "Avenida Libertador, frente al Hospital Central",
    "ciudad": "Caracas"
  },
  "descripcionOriginal": "Accidente de tráfico grave. Colisión entre dos vehículos. Hay una persona inconsciente con hemorragia en la cabeza. Necesita atención urgente.",
  "tipoIncidenteReportado": "ACCIDENTE_TRAFICO"
}
```

**Respuesta esperada:**
```json
{
  "id": "abc-123-def-456",
  "estadoIncidente": "RECIBIDO",
  "prioridadInicial": 3,
  "fechaReporte": "2025-11-05T..."
}
```

**🤖 En este momento el análisis ML está corriendo automáticamente en segundo plano**

---

**2. Esperar 3-4 segundos**

Tomar un café ☕ mientras el servicio ML analiza el texto...

---

**3. Verificar el Análisis de Texto**

```
GET http://localhost:8080/api/incidentes/{ID-del-incidente}/detalle
```

**Respuesta esperada con análisis completado:**
```json
{
  "id": "abc-123-def-456",
  "estadoIncidente": "ANALIZADO",  // ← Cambió automáticamente
  "descripcionOriginal": "Accidente de tráfico grave...",
  "prioridadInicial": 3,
  "prioridadTexto": 1,  // ← CALCULADO AUTOMÁTICAMENTE
  "tipoIncidenteClasificado": "Crítico - Riesgo vital",  // ← CLASIFICADO AUTOMÁTICAMENTE
  "solicitante": { ... },
  "ubicacion": { ... },
  "analisisTexto": {  // ← RESULTADO DEL ANÁLISIS ML
    "id": "...",
    "prioridadCalculada": 1,
    "nivelGravedad": 5,
    "tipoIncidentePredicho": "Crítico - Riesgo vital inmediato",
    "scoreConfianza": 0.92,
    "palabrasClaveCriticas": {
      "palabras": ["accidente", "grave", "inconsciente", "hemorragia", "urgente"]
    },
    "categoriasDetectadas": { ... },
    "entidadesMedicas": { ... }
  }
}
```

**✅ ¡El análisis se ejecutó AUTOMÁTICAMENTE!**

---

### **Paso 3: Probar Subida de Imagen con Análisis Automático**

**4. Subir una Imagen**

```
POST http://localhost:8080/api/multimedia/incidente/{ID-del-incidente}/subir
Content-Type: multipart/form-data

archivo: [Seleccionar archivo de imagen]
descripcion: Foto del accidente mostrando los vehículos
esPrincipal: true
```

**Respuesta esperada:**
```json
{
  "id": "img-789-xyz",
  "urlArchivo": "./uploads/uuid-random.jpg",
  "tipoArchivo": "IMAGEN",
  "requiereAnalisisMl": true,
  "analisisCompletado": false
}
```

**🤖 En este momento el análisis de imagen está corriendo automáticamente en segundo plano**

---

**5. Esperar 5-6 segundos**

Tomar otro café ☕☕ mientras el servicio ML analiza la imagen...

---

**6. Verificar el Análisis Completo**

```
GET http://localhost:8080/api/incidentes/{ID-del-incidente}/detalle
```

**Respuesta esperada con análisis completo:**
```json
{
  "id": "abc-123-def-456",
  "estadoIncidente": "ANALIZADO",
  "descripcionOriginal": "Accidente de tráfico grave...",

  // Análisis de texto
  "prioridadTexto": 1,
  "tipoIncidenteClasificado": "Crítico - Riesgo vital",

  // Análisis de imagen - CALCULADO AUTOMÁTICAMENTE
  "prioridadImagen": 4,  // ← NUEVO
  "scoreVeracidad": 0.88,  // ← NUEVO

  // Prioridad final combinada - CALCULADO AUTOMÁTICAMENTE
  "prioridadFinal": 2,  // ← CALCULADO: (1×0.6 + 4×0.4) = 2.2 ≈ 2

  "multimedia": [
    {
      "id": "img-789-xyz",
      "analisisCompletado": true,  // ← Cambió a true
      "analisisImagen": {  // ← RESULTADO DEL ANÁLISIS ML
        "esImagenAccidente": true,
        "nivelGravedadVisual": 4,
        "scoreVeracidad": 0.88,
        "tipoEscenaDetectada": "Accidente vehicular",
        "objetosDetectados": {
          "vehiculos": 2,
          "personas": 1
        },
        "personasDetectadas": 1,
        "vehiculosDetectados": 2,
        "scoreConfianzaEscena": 0.85,
        "esAnomalia": false,
        "calidadImagen": "BUENA",
        "esImagenClara": true
      }
    }
  ]
}
```

**✅ ¡El análisis de imagen también se ejecutó AUTOMÁTICAMENTE!**

---

## 📊 Flujo Completo Verificado

```
1. Usuario crea incidente
   ↓ [Respuesta inmediata]

2. 🤖 Sistema analiza texto automáticamente (2-3s)
   ↓ [En segundo plano con @Async]

3. Usuario sube imagen
   ↓ [Respuesta inmediata]

4. 🤖 Sistema analiza imagen automáticamente (4-5s)
   ↓ [En segundo plano con @Async]

5. 🤖 Sistema calcula prioridad final automáticamente
   ↓ [Instantáneo]

6. ✅ Incidente completamente analizado y listo
```

---

## 🎯 Puntos Clave a Observar

### ✅ **Lo que el usuario hace:**
1. Crear incidente (recibe respuesta inmediata)
2. Subir imagen (recibe respuesta inmediata)

### 🤖 **Lo que el sistema hace AUTOMÁTICAMENTE:**
1. Analizar texto en segundo plano
2. Calcular prioridad de texto
3. Cambiar estado del incidente
4. Analizar imagen en segundo plano
5. Calcular severidad visual
6. Calcular score de veracidad
7. Calcular prioridad final combinada
8. Actualizar estado del incidente

**Todo sin intervención manual del usuario** ✨

---

## 📝 Logs a Observar

En la consola del microservicio Java verás logs como:

```
INFO c.r.r.s.impl.IncidenteServiceImpl : Incidente creado exitosamente con ID: abc-123
INFO c.r.r.s.i.AnalisisMlOrchestrationServiceImpl : Iniciando análisis automático de texto para incidente: abc-123
INFO c.r.recepcion.client.MlTextoClient : Llamando al servicio ML de texto: http://localhost:8001
INFO c.r.recepcion.client.MlTextoClient : Análisis de texto completado exitosamente. Prioridad: 1
INFO c.r.r.s.i.AnalisisMlOrchestrationServiceImpl : Análisis de texto completado exitosamente. Prioridad: 1
INFO c.r.r.s.i.AnalisisMlOrchestrationServiceImpl : Prioridad final calculada: 1 (Texto: 1, Imagen: null)
```

Y al subir imagen:

```
INFO c.r.r.s.impl.MultimediaServiceImpl : Archivo subido exitosamente con ID: img-789
INFO c.r.r.s.i.AnalisisMlOrchestrationServiceImpl : Iniciando análisis automático de imagen para multimedia: img-789
INFO c.r.recepcion.client.MlImagenClient : Llamando al servicio ML de imagen: http://localhost:8002
INFO c.r.recepcion.client.MlImagenClient : Análisis de imagen completado. Es accidente: true, Severidad: 4
INFO c.r.r.s.i.AnalisisMlOrchestrationServiceImpl : Análisis de imagen completado exitosamente. Severidad: 4, Veracidad: 0.88
INFO c.r.r.s.i.AnalisisMlOrchestrationServiceImpl : Prioridad final calculada: 2 (Texto: 1, Imagen: 4)
```

---

## 🐛 Troubleshooting

### Problema: "Connection refused" al crear incidente

**Causa:** Servicios ML no están corriendo

**Solución:**
```bash
# Verificar servicios
curl http://localhost:8001/api/ml/salud
curl http://localhost:8002/api/ml/salud

# Si no responden, iniciarlos según Paso 1
```

---

### Problema: El análisis no se completa

**Causa:** El servicio ML tardó más de lo esperado

**Solución:**
- Esperar un poco más (hasta 10 segundos)
- Verificar logs del microservicio Java
- Verificar logs de los servicios Python ML

---

### Problema: prioridadTexto es null después de esperar

**Causa:** El servicio ML falló o no está disponible

**Solución:**
1. Verificar que el servicio ML esté corriendo: `curl http://localhost:8001/api/ml/salud`
2. Verificar que el modelo esté entrenado: `python train_model.py`
3. Revisar logs del servicio ML Python

---

## 📚 Documentos Relacionados

- **ANALISIS_ML_AUTOMATICO.md** - Documentación técnica completa
- **VERIFICACION_SISTEMA.md** - Estado del sistema
- **CORRECCIONES_REALIZADAS.md** - Correcciones aplicadas
- **QUICK_START.md** - Guía de inicio rápido

---

## ✅ Checklist de Verificación

- [ ] Servicio ML Texto corriendo (puerto 8001)
- [ ] Servicio ML Imagen corriendo (puerto 8002)
- [ ] Microservicio Java corriendo (puerto 8080)
- [ ] Incidente creado correctamente
- [ ] Análisis de texto completado (prioridadTexto != null)
- [ ] Imagen subida correctamente
- [ ] Análisis de imagen completado (prioridadImagen != null)
- [ ] Prioridad final calculada (prioridadFinal != null)

---

**🎉 ¡Sistema completamente operativo con análisis ML automático!**
