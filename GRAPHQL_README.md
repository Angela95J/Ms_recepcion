# 🚀 GraphQL Implementation Guide

## ✅ Estado de Implementación

### **Completado:**
- ✅ Dependencias de GraphQL agregadas al `pom.xml`
- ✅ Esquema GraphQL completo (`schema.graphqls`)
- ✅ Configuración en `application.yml`
- ✅ Scalars personalizados (UUID, DateTime, BigDecimal, JSON, Long)

### **Pendiente (Próximos pasos):**
- ⏳ Implementar Query Resolvers
- ⏳ Implementar Field Resolvers para relaciones
- ⏳ Configurar DataLoaders (prevenir N+1)
- ⏳ Configurar seguridad (API Keys)
- ⏳ Testing de GraphQL

---

## 📊 **Arquitectura GraphQL Implementada**

```
┌─────────────────────────────────────────────────┐
│          Microservicio Spring Boot              │
├─────────────────────────────────────────────────┤
│                                                 │
│  REST API (/api/*)  +  GraphQL API (/graphql)   │
│                                                 │
│  ┌──────────────────────────────────────────┐  │
│  │  GraphQL Layer                           │  │
│  │  ├─ Schema (schema.graphqls)             │  │
│  │  ├─ Scalars Config (UUID, DateTime, etc)│  │
│  │  ├─ Query Resolvers (TODO)              │  │
│  │  ├─ Field Resolvers (TODO)              │  │
│  │  └─ DataLoaders (TODO)                  │  │
│  └──────────────┬───────────────────────────┘  │
│                 │                               │
│  ┌──────────────▼───────────────────────────┐  │
│  │  Services (existentes)                   │  │
│  │  - IncidenteService                      │  │
│  │  - SolicitanteService                    │  │
│  │  - UbicacionService                      │  │
│  │  - etc.                                  │  │
│  └──────────────┬───────────────────────────┘  │
│                 │                               │
│  ┌──────────────▼───────────────────────────┐  │
│  │  Repository Layer                        │  │
│  └──────────────┬───────────────────────────┘  │
│                 │                               │
│         ┌───────▼────────┐                      │
│         │   PostgreSQL   │                      │
│         └────────────────┘                      │
└─────────────────────────────────────────────────┘
```

---

## 🔧 **Archivos Creados**

### 1. **Dependencias (`pom.xml`)**
```xml
<!-- GraphQL -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>
<dependency>
    <groupId>com.graphql-java</groupId>
    <artifactId>graphql-java-extended-scalars</artifactId>
    <version>21.0</version>
</dependency>

<!-- Testing GraphQL -->
<dependency>
    <groupId>org.springframework.graphql</groupId>
    <artifactId>spring-graphql-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 2. **Esquema GraphQL (`src/main/resources/graphql/schema.graphqls`)**
Contiene:
- **Scalars**: UUID, DateTime, BigDecimal, Long, JSON
- **Enums**: EstadoIncidente, CanalOrigen, TipoArchivo, etc.
- **Types**: Incidente, Solicitante, Ubicacion, Multimedia, Analisis ML, etc.
- **Inputs**: Filtros y paginación
- **Queries**: 20+ queries para consultar datos

### 3. **Configuración (`application.yml`)**
```yaml
spring.graphql:
  graphiql:
    enabled: true
    path: /graphiql
  path: /graphql
  schema:
    locations: classpath:graphql/
    file-extensions: .graphqls,.graphql
  cors:
    allowed-origins: "*"
    allowed-methods: "*"
```

### 4. **Scalars Config (`GraphQLScalarConfig.java`)**
Configuración de tipos personalizados:
- UUID
- DateTime (LocalDateTime con ISO-8601)
- BigDecimal (de ExtendedScalars)
- JSON (de ExtendedScalars)
- Long (de ExtendedScalars)

---

## 🚀 **Acceso a GraphQL**

### **GraphiQL (Interfaz de Testing)**
**URL:** http://localhost:8080/api/graphiql

GraphiQL es una interfaz web interactiva para probar queries GraphQL.

### **Endpoint GraphQL**
**URL:** http://localhost:8080/api/graphql

Para clientes programáticos (n8n, otros microservicios, apps móviles).

---

## 📝 **Ejemplos de Queries**

### **Query 1: Obtener un Incidente por ID**
```graphql
query GetIncidente($id: UUID!) {
  incidente(id: $id) {
    id
    codigo
    descripcionOriginal
    prioridadFinal
    estadoIncidente
    fechaReporte

    solicitante {
      nombreCompleto
      telefono
    }

    ubicacion {
      descripcionTextual
      latitud
      longitud
      distrito
    }
  }
}

# Variables:
{
  "id": "123e4567-e89b-12d3-a456-426614174000"
}
```

### **Query 2: Listar Incidentes con Filtros**
```graphql
query ListarIncidentes {
  incidentes(
    filtros: {
      estado: APROBADO
      prioridadMin: 3
      distrito: "Cercado"
    }
    paginacion: {
      page: 0
      size: 10
      orderBy: "fechaReporte"
      direction: DESC
    }
  ) {
    content {
      id
      codigo
      descripcionOriginal
      prioridadFinal
      estadoIncidente
      solicitante {
        nombreCompleto
      }
    }
    totalElements
    totalPages
    number
  }
}
```

### **Query 3: Dashboard (Múltiples Queries en Una)**
```graphql
query Dashboard {
  # Incidentes recientes
  incidentes(paginacion: { page: 0, size: 5 }) {
    content {
      id
      codigo
      prioridadFinal
      estadoIncidente
    }
    totalElements
  }

  # Incidentes de prioridad alta
  incidentesPrioridadAlta(paginacion: { page: 0, size: 10 }) {
    content {
      id
      codigo
      descripcionOriginal
      prioridadFinal
    }
  }

  # Estadísticas
  estadisticasIncidentes {
    totalIncidentes
    porEstado {
      estado
      cantidad
      porcentaje
    }
    porPrioridad {
      prioridad
      cantidad
    }
  }
}
```

### **Query 4: Para App Móvil (Solo Datos Necesarios)**
```graphql
query IncidenteMovil($id: UUID!) {
  incidente(id: $id) {
    codigo
    descripcionOriginal
    prioridadFinal
    estadoIncidente
    fechaReporte

    solicitante {
      nombreCompleto
    }

    ubicacion {
      descripcionTextual
      latitud
      longitud
    }

    # Solo miniaturas de multimedia
    multimedia {
      urlMiniatura
      tipoArchivo
    }
  }
}
```

### **Query 5: Para n8n Bot**
```graphql
query IncidenteBot($id: UUID!) {
  incidente(id: $id) {
    codigo
    prioridadFinal
    estadoIncidente
  }
}
```

---

## 📦 **Próximos Pasos de Implementación**

### **Paso 1: Implementar Query Resolvers**

Crear: `src/main/java/com/recepcion/recepcion/graphql/resolver/IncidenteQueryResolver.java`

```java
@Controller
public class IncidenteQueryResolver {

    @Autowired
    private IncidenteService incidenteService;

    @QueryMapping
    public IncidenteResponse incidente(@Argument UUID id) {
        return incidenteService.buscarPorId(id);
    }

    @QueryMapping
    public Page<IncidenteResponse> incidentes(
        @Argument IncidenteFilter filtros,
        @Argument PageInput paginacion
    ) {
        // Implementar lógica
    }
}
```

### **Paso 2: Implementar Field Resolvers**

Para resolver relaciones lazy (evitar N+1):

```java
@Controller
public class IncidenteFieldResolver {

    @Autowired
    private SolicitanteService solicitanteService;

    @SchemaMapping(typeName = "Incidente", field = "solicitante")
    public SolicitanteResponse solicitante(IncidenteResponse incidente) {
        return solicitanteService.buscarPorId(incidente.getSolicitanteId());
    }
}
```

### **Paso 3: Configurar DataLoaders**

Para cargar datos en batch y evitar N+1:

```java
@Configuration
public class DataLoaderConfig {

    @Bean
    public BatchLoaderRegistry batchLoaderRegistry(SolicitanteService service) {
        return registry -> registry.forTypePair(
            UUID.class,
            SolicitanteResponse.class
        ).registerMappedBatchLoader((ids, env) -> {
            return service.buscarPorIds(ids);
        });
    }
}
```

### **Paso 4: Configurar Seguridad**

Reusar el sistema de API Keys existente:

```java
@Configuration
public class GraphQLSecurityConfig {

    @Bean
    public DataFetcherInterceptor apiKeyInterceptor() {
        return (dataFetcherContext, chain) -> {
            // Validar API Key desde header
            // Reusar SecurityConfigSimple existente
        };
    }
}
```

---

## 🧪 **Testing**

### **Test con GraphiQL**
1. Abrir: http://localhost:8080/api/graphiql
2. Escribir query
3. Ejecutar y ver resultados

### **Test con curl**
```bash
curl -X POST http://localhost:8080/api/graphql \
  -H "Content-Type: application/json" \
  -H "X-API-Key: admin-key-change-in-production-12345" \
  -d '{
    "query": "query { incidente(id: \"...\") { codigo descripcionOriginal } }"
  }'
```

### **Test Unitario**
```java
@SpringBootTest
@AutoConfigureGraphQlTester
class IncidenteGraphQLTest {

    @Autowired
    GraphQlTester graphQlTester;

    @Test
    void testIncidenteQuery() {
        graphQlTester.document("""
            query GetIncidente($id: UUID!) {
                incidente(id: $id) {
                    id
                    codigo
                }
            }
            """)
            .variable("id", UUID.randomUUID())
            .execute()
            .path("incidente.codigo").hasValue();
    }
}
```

---

## 🔗 **Integración con n8n**

### **Workflow n8n con GraphQL**

```
1. Trigger: Mensaje WhatsApp/Telegram
   ↓
2. HTTP Request Node → POST /graphql
   Headers:
     Content-Type: application/json
     X-API-Key: n8n-key-change-in-production-67890
   Body:
   {
     "query": "query GetIncidente($id: UUID!) { incidente(id: $id) { codigo prioridadFinal estadoIncidente } }",
     "variables": { "id": "{{$json.incidenteId}}" }
   }
   ↓
3. Function Node: Extraer datos de response.data.incidente
   ↓
4. Telegram/WhatsApp Send Message
   Mensaje: "✅ Código: {{codigo}}, Prioridad: {{prioridadFinal}}"
```

---

## 📚 **Beneficios de GraphQL para Otros Microservicios**

### **1. Flexibilidad de Queries**
Otros microservicios pueden pedir exactamente lo que necesitan:

```graphql
# Microservicio de Despacho solo necesita ubicación y prioridad
query ParaDespacho {
  incidentesParaDespacho {
    content {
      id
      prioridadFinal
      ubicacion { latitud longitud distrito }
    }
  }
}

# Microservicio de Reportes necesita estadísticas
query ParaReportes {
  estadisticasIncidentes(
    filtros: { fechaInicio: "2025-01-01", fechaFin: "2025-01-31" }
  ) {
    totalIncidentes
    porDistrito { distrito cantidad }
  }
}
```

### **2. Sin Over-fetching**
- REST: Cada endpoint retorna estructura fija → desperdicio de datos
- GraphQL: Cliente especifica campos → solo lo necesario

### **3. Sin Under-fetching**
- REST: Múltiples llamadas para datos relacionados
- GraphQL: Una query con todos los datos relacionados

### **4. Versionamiento Implícito**
- Agregar campos nuevos no rompe clientes existentes
- Deprecar campos gradualmente
- No necesitas `/v1/`, `/v2/` endpoints

---

## ⚡ **Comandos Útiles**

```bash
# Compilar proyecto con nuevas dependencias
mvn clean install

# Iniciar microservicio
mvn spring-boot:run

# Acceder a GraphiQL
http://localhost:8080/api/graphiql

# Ver esquema GraphQL
http://localhost:8080/api/graphql/schema
```

---

## 📖 **Recursos Adicionales**

- [Spring for GraphQL Docs](https://docs.spring.io/spring-graphql/docs/current/reference/html/)
- [GraphQL Java Docs](https://www.graphql-java.com/documentation/master/)
- [GraphQL Extended Scalars](https://github.com/graphql-java/graphql-java-extended-scalars)
- [DataLoader Pattern](https://github.com/graphql-java/java-dataloader)

---

**Estado:** Implementación base completada, falta implementar Resolvers
**Próximo paso:** Implementar Query Resolvers usando los Services existentes

**Última actualización:** 2025-01-07
