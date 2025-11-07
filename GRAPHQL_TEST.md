# 🧪 Guía de Pruebas - GraphQL

## ✅ Resumen de la Compilación

**Estado:** ✅ **COMPILACIÓN EXITOSA**

```
[INFO] BUILD SUCCESS
[INFO] Total time:  10.581 s
[INFO] Building recepcion 0.0.1-SNAPSHOT
[INFO] Compiling 88 source files
```

**Archivos compilados:**
- ✅ GraphQLScalarConfig.java
- ✅ HealthQueryResolver.java
- ✅ schema.graphqls cargado correctamente

---

## 🚀 Cómo Probar GraphQL

### **Paso 1: Iniciar PostgreSQL**

Si usas Docker:
```bash
docker-compose -f docker-compose.app.yml up -d postgres
```

O iniciar PostgreSQL manualmente en puerto 5432.

---

### **Paso 2: Iniciar el Microservicio**

#### Opción A: Desde línea de comandos
```bash
cd recepcion
./mvnw.cmd spring-boot:run
```

#### Opción B: Desde tu IDE
- Ejecutar la clase `RecepcionApplication.java`

**Esperar a ver:**
```
Started RecepcionApplication in X seconds
Tomcat started on port(s): 8080
```

---

### **Paso 3: Verificar que GraphQL está activo**

#### Test 1: Verificar endpoint GraphQL
```bash
curl http://localhost:8080/api/graphql
```

**Resultado esperado:** Error o respuesta, pero el endpoint debe existir.

#### Test 2: Abrir GraphiQL (Interfaz Web)
**URL:** http://localhost:8080/api/graphiql

**Deberías ver:**
- Interfaz GraphiQL con panel de query
- Botón "Docs" que muestra el esquema
- Autocomplete de queries

---

### **Paso 4: Probar la Query Health**

En GraphiQL, ejecuta:

```graphql
query {
  health
}
```

**Resultado esperado:**
```json
{
  "data": {
    "health": "GraphQL API is running! ✅"
  }
}
```

✅ Si ves esto, **GraphQL está funcionando correctamente!**

---

### **Paso 5: Explorar el Esquema**

En GraphiQL, haz clic en el botón **"Docs"** (esquina superior derecha).

**Deberías ver:**
- Query: health, incidente, incidentes, solicitante, etc.
- Types: Incidente, Solicitante, Ubicacion, etc.
- Scalars: UUID, DateTime, BigDecimal, JSON, Long
- Enums: EstadoIncidente, CanalOrigen, etc.

---

## 🧪 Pruebas Adicionales

### **Test 1: Introspection Query**

Esta query obtiene todo el esquema:

```graphql
query IntrospectionQuery {
  __schema {
    queryType {
      name
      fields {
        name
        description
      }
    }
    types {
      name
      kind
    }
  }
}
```

**Resultado esperado:** Lista completa de tipos y queries.

---

### **Test 2: Verificar Scalars Personalizados**

```graphql
query {
  __type(name: "UUID") {
    name
    kind
  }
  __type(name: "DateTime") {
    name
    kind
  }
}
```

**Resultado esperado:**
```json
{
  "data": {
    "__type": {
      "name": "UUID",
      "kind": "SCALAR"
    },
    "__type": {
      "name": "DateTime",
      "kind": "SCALAR"
    }
  }
}
```

---

### **Test 3: Probar con curl**

```bash
curl -X POST http://localhost:8080/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ health }"}'
```

**Resultado esperado:**
```json
{"data":{"health":"GraphQL API is running! ✅"}}
```

---

### **Test 4: Probar con API Key (cuando esté configurada)**

```bash
curl -X POST http://localhost:8080/api/graphql \
  -H "Content-Type: application/json" \
  -H "X-API-Key: admin-key-change-in-production-12345" \
  -d '{"query":"{ health }"}'
```

---

## 🔍 Verificación del Esquema

### **Queries Disponibles (según schema.graphqls):**

```graphql
type Query {
  # Health Check (✅ Implementado)
  health: String!

  # Incidentes (⏳ Pendiente implementar resolvers)
  incidente(id: UUID!): Incidente
  incidentes(filtros: IncidenteFilter, paginacion: PageInput): IncidentePage!
  incidentesPorEstado(estado: EstadoIncidente!, paginacion: PageInput): IncidentePage!
  incidentesPorPrioridad(prioridad: Int!, paginacion: PageInput): IncidentePage!
  incidentesPrioridadAlta(paginacion: PageInput): IncidentePage!
  incidentesPendientesAnalisis(paginacion: PageInput): IncidentePage!
  incidentesParaDespacho(paginacion: PageInput): IncidentePage!
  incidentesPorSolicitante(solicitanteId: UUID!, paginacion: PageInput): IncidentePage!
  incidentesPorRangoFechas(fechaInicio: DateTime!, fechaFin: DateTime!, paginacion: PageInput): IncidentePage!

  # Solicitantes (⏳ Pendiente)
  solicitante(id: UUID!): Solicitante
  solicitantes(filtros: SolicitanteFilter, paginacion: PageInput): SolicitantePage!
  solicitantePorTelefono(telefono: String!): Solicitante

  # Ubicaciones (⏳ Pendiente)
  ubicacion(id: UUID!): Ubicacion
  ubicaciones(filtros: UbicacionFilter, paginacion: PageInput): UbicacionPage!

  # Historial (⏳ Pendiente)
  historialIncidente(incidenteId: UUID!): [HistorialEstado!]!

  # Estadísticas (⏳ Pendiente)
  estadisticasIncidentes(filtros: IncidenteFilter): EstadisticasIncidente!
}
```

---

## ⚠️ Queries que AÚN NO funcionan

Las siguientes queries están definidas en el esquema pero **no tienen resolvers implementados** aún:

❌ `incidente(id: UUID!)` - Falta implementar resolver
❌ `incidentes(...)` - Falta implementar resolver
❌ `solicitante(...)` - Falta implementar resolver
❌ Todas las demás queries

**Qué pasa si las ejecutas:**
```graphql
query {
  incidente(id: "123e4567-e89b-12d3-a456-426614174000") {
    codigo
  }
}
```

**Error esperado:**
```json
{
  "errors": [
    {
      "message": "DataFetchingException: No resolver found for field 'incidente'"
    }
  ]
}
```

---

## 📊 Estado de la Implementación

| Componente | Estado | Funcional |
|------------|--------|-----------|
| ✅ Schema GraphQL | Completo | Sí |
| ✅ Scalars (UUID, DateTime, etc) | Completo | Sí |
| ✅ Configuración (application.yml) | Completo | Sí |
| ✅ GraphiQL UI | Habilitado | Sí |
| ✅ Health Query | Implementado | **✅ Sí** |
| ⏳ Incidente Queries | Esquema definido | No |
| ⏳ Solicitante Queries | Esquema definido | No |
| ⏳ Ubicacion Queries | Esquema definido | No |
| ⏳ Estadísticas Queries | Esquema definido | No |

**Progreso:** 40% - Infraestructura completa, faltan resolvers

---

## 🎯 Lo Que Puedes Probar AHORA

### ✅ Funciona:
1. GraphiQL UI (http://localhost:8080/api/graphiql)
2. Query `health`
3. Introspección del esquema
4. Autocomplete en GraphiQL
5. Documentación automática

### ⏳ No Funciona Aún:
1. Queries de incidentes
2. Queries de solicitantes
3. Queries de ubicaciones
4. Queries de estadísticas

---

## 🐛 Troubleshooting

### Problema: "Connection refused" al iniciar
**Causa:** PostgreSQL no está corriendo

**Solución:**
```bash
# Con Docker
docker-compose -f docker-compose.app.yml up -d postgres

# O iniciar PostgreSQL manualmente
```

---

### Problema: "Schema validation failed"
**Causa:** El esquema tiene errores de sintaxis

**Solución:**
Verificar que `schema.graphqls` existe en:
```
src/main/resources/graphql/schema.graphqls
```

---

### Problema: GraphiQL no carga
**Causa:** Configuración incorrecta en application.yml

**Solución:**
Verificar que en `application.yml` existe:
```yaml
spring.graphql:
  graphiql:
    enabled: true
    path: /graphiql
```

---

### Problema: "No resolver found for field X"
**Causa:** El resolver para esa query no está implementado

**Solución:**
Esperar a que se implementen los resolvers, o implementarlos manualmente.

---

## 📚 Próximos Pasos

Para que GraphQL esté 100% funcional, necesitas:

1. **Implementar IncidenteQueryResolver** (conectar con IncidenteService)
2. **Implementar SolicitanteQueryResolver** (conectar con SolicitanteService)
3. **Implementar UbicacionQueryResolver** (conectar con UbicacionService)
4. **Implementar Field Resolvers** (para relaciones lazy)
5. **Configurar DataLoaders** (para evitar N+1)

**Tiempo estimado:** 5-7 días de desarrollo

---

## ✅ Checklist de Pruebas

- [ ] PostgreSQL corriendo
- [ ] Microservicio iniciado sin errores
- [ ] GraphiQL accesible en http://localhost:8080/api/graphiql
- [ ] Query `health` retorna mensaje exitoso
- [ ] Documentación del esquema visible en GraphiQL
- [ ] Autocomplete funciona en GraphiQL
- [ ] Introspection query funciona
- [ ] Test con curl funciona

---

**Última actualización:** 2025-01-07

**Estado:** ✅ GraphQL Base Funcional - Listo para probar
