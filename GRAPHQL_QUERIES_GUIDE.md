# Guía de Queries GraphQL para Microservicios

## Índice
1. [Introducción](#introducción)
2. [Configuración](#configuración)
3. [Queries para Microservicio de Despacho](#queries-para-microservicio-de-despacho)
4. [Queries para Microservicio Frontend](#queries-para-microservicio-frontend)
5. [Queries para n8n](#queries-para-n8n)
6. [Tipos de Datos](#tipos-de-datos)
7. [Paginación](#paginación)
8. [Ejemplos de Uso](#ejemplos-de-uso)

---

## Introducción

Este microservicio de recepción de incidentes expone una API GraphQL completa para:
- **Microservicio de Despacho de Ambulancias**: Consultar incidentes listos para despacho
- **Microservicio Frontend**: Gestión completa de incidentes, solicitantes y ubicaciones
- **n8n**: Verificación de solicitantes y creación de incidentes vía bot

**Endpoint GraphQL**: `http://localhost:8080/graphql`
**GraphiQL UI**: `http://localhost:8080/graphiql`

---

## Configuración

### Conexión desde otro Microservicio (Java/Spring Boot)

```java
@Configuration
public class GraphQLClientConfig {

    @Bean
    public WebClient graphqlWebClient() {
        return WebClient.builder()
            .baseUrl("http://ms-recepcion:8080/graphql")
            .defaultHeader("Content-Type", "application/json")
            .build();
    }
}
```

### Conexión desde n8n

En n8n, usar el nodo **HTTP Request** con:
- **Method**: POST
- **URL**: `http://ms-recepcion:8080/graphql`
- **Body Content Type**: JSON
- **Body**:
```json
{
  "query": "query { ... }",
  "variables": { ... }
}
```

---

## Queries para Microservicio de Despacho

### 1. Incidentes Listos para Despacho

**USO**: Obtener incidentes con estado APROBADO y ANALIZADO_ML, ordenados por prioridad.

```graphql
query IncidentesParaDespacho {
  incidentesParaDespacho(paginacion: {
    page: 0
    size: 50
    orderBy: "prioridadFinal"
    direction: DESC
  }) {
    content {
      id
      descripcion
      prioridadFinal
      estado
      fechaReporte
      esVerosimil
      ubicacion {
        id
        latitud
        longitud
        direccion
        distrito
      }
      solicitante {
        id
        nombre
        telefono
      }
      analisisMlTexto {
        prioridadPredecida
      }
    }
    totalElements
    totalPages
    number
    size
  }
}
```

**Respuesta típica**:
```json
{
  "data": {
    "incidentesParaDespacho": {
      "content": [
        {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "descripcion": "Accidente de tránsito grave",
          "prioridadFinal": 5,
          "estado": "APROBADO",
          "fechaReporte": "2025-11-07T10:30:00",
          "esVerosimil": true,
          "ubicacion": {
            "latitud": -12.046374,
            "longitud": -77.042793,
            "direccion": "Av. Arequipa 1234",
            "distrito": "Miraflores"
          },
          "solicitante": {
            "nombre": "Juan Pérez",
            "telefono": "+51987654321"
          }
        }
      ],
      "totalElements": 15,
      "totalPages": 1,
      "number": 0,
      "size": 50
    }
  }
}
```

### 2. Incidentes de Prioridad Alta

**USO**: Solo incidentes urgentes (prioridad >= 3).

```graphql
query IncidentesPrioridadAlta {
  incidentesPrioridadAlta(paginacion: {
    page: 0
    size: 20
    orderBy: "prioridadFinal"
    direction: DESC
  }) {
    content {
      id
      descripcion
      prioridadFinal
      estado
      ubicacion {
        latitud
        longitud
        direccion
      }
    }
    totalElements
  }
}
```

### 3. Detalle Completo de Incidente

**USO**: Obtener todos los datos de un incidente para el despacho.

```graphql
query DetalleIncidente($id: UUID!) {
  incidente(id: $id) {
    id
    descripcion
    prioridadInicial
    prioridadFinal
    estado
    estadoValidacion
    fechaReporte
    esVerosimil
    motivoRechazo

    solicitante {
      id
      nombre
      telefono
      documento
      tipoDocumento
    }

    ubicacion {
      id
      latitud
      longitud
      direccion
      distrito
      referencia
    }

    multimedia {
      id
      urlArchivo
      tipoArchivo
      descripcion
    }

    analisisMlTexto {
      id
      prioridadPredecida
      cluster
      confianza
      palabrasClave
      fechaAnalisis
    }

    analisisMlImagen {
      id
      esVerosimil
      confianza
      caracteristicasDetectadas
      fechaAnalisis
    }
  }
}
```

**Variables**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## Queries para Microservicio Frontend

### 1. Dashboard - Listar Todos los Incidentes

```graphql
query Dashboard {
  incidentes(
    paginacion: {
      page: 0
      size: 20
      orderBy: "fechaReporte"
      direction: DESC
    }
  ) {
    content {
      id
      descripcion
      prioridadFinal
      estado
      estadoValidacion
      fechaReporte
      solicitante {
        nombre
        telefono
      }
      ubicacion {
        distrito
      }
    }
    totalElements
    totalPages
    number
    size
  }
}
```

### 2. Filtrar Incidentes por Estado

```graphql
query IncidentesPorEstado($estado: EstadoIncidente!) {
  incidentesPorEstado(
    estado: $estado
    paginacion: {
      page: 0
      size: 20
    }
  ) {
    content {
      id
      descripcion
      prioridadFinal
      estado
      fechaReporte
    }
    totalElements
  }
}
```

**Variables**:
```json
{
  "estado": "APROBADO"
}
```

**Valores posibles para EstadoIncidente**:
- `PENDIENTE`
- `APROBADO`
- `RECHAZADO`
- `EN_ATENCION`
- `ATENDIDO`
- `CANCELADO`

### 3. Filtrar Incidentes con Múltiples Filtros

```graphql
query FiltrarIncidentes($filtros: IncidenteFilterInput!) {
  incidentes(
    filtros: $filtros
    paginacion: { page: 0, size: 20 }
  ) {
    content {
      id
      descripcion
      prioridadFinal
      estado
      fechaReporte
      ubicacion {
        distrito
      }
    }
    totalElements
  }
}
```

**Variables - Ejemplo 1 (Por Estado)**:
```json
{
  "filtros": {
    "estado": "APROBADO"
  }
}
```

**Variables - Ejemplo 2 (Por Rango de Fechas)**:
```json
{
  "filtros": {
    "fechaInicio": "2025-11-01T00:00:00",
    "fechaFin": "2025-11-07T23:59:59"
  }
}
```

**Variables - Ejemplo 3 (Por Solicitante)**:
```json
{
  "filtros": {
    "solicitanteId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

### 4. Panel de Administración ML

**USO**: Ver incidentes pendientes de análisis por ML.

```graphql
query PanelAdminML {
  incidentesPendientesAnalisis(paginacion: {
    page: 0
    size: 20
    orderBy: "fechaReporte"
    direction: ASC
  }) {
    content {
      id
      descripcion
      prioridadInicial
      estado
      estadoValidacion
      fechaReporte
      solicitante {
        nombre
      }
    }
    totalElements
  }
}
```

### 5. Historial de Solicitante

```graphql
query HistorialSolicitante($solicitanteId: UUID!) {
  incidentesPorSolicitante(
    solicitanteId: $solicitanteId
    paginacion: { page: 0, size: 50 }
  ) {
    content {
      id
      descripcion
      estado
      prioridadFinal
      fechaReporte
      esVerosimil
    }
    totalElements
  }
}
```

### 6. Gestión de Solicitantes

```graphql
query ListarSolicitantes {
  solicitantes(paginacion: {
    page: 0
    size: 20
  }) {
    content {
      id
      nombre
      telefono
      email
      tipoDocumento
      documento
      canalOrigen
      fechaRegistro
    }
    totalElements
    totalPages
  }
}
```

### 7. Gestión de Ubicaciones

```graphql
query ListarUbicaciones {
  ubicaciones(paginacion: {
    page: 0
    size: 20
  }) {
    content {
      id
      latitud
      longitud
      direccion
      distrito
      referencia
      fechaCreacion
    }
    totalElements
  }
}
```

### 8. Reportes por Rango de Fechas

```graphql
query ReporteMensual(
  $fechaInicio: DateTime!
  $fechaFin: DateTime!
) {
  incidentesPorRangoFechas(
    fechaInicio: $fechaInicio
    fechaFin: $fechaFin
    paginacion: { page: 0, size: 100 }
  ) {
    content {
      id
      descripcion
      estado
      prioridadFinal
      fechaReporte
      ubicacion {
        distrito
      }
    }
    totalElements
  }
}
```

**Variables**:
```json
{
  "fechaInicio": "2025-11-01T00:00:00",
  "fechaFin": "2025-11-30T23:59:59"
}
```

---

## Queries para n8n

### 1. Verificar si Solicitante Existe

**USO**: Antes de crear un incidente desde el bot, verificar si el solicitante ya está registrado.

```graphql
query VerificarSolicitante($telefono: String!) {
  solicitantePorTelefono(telefono: $telefono) {
    id
    nombre
    telefono
    email
  }
}
```

**Variables**:
```json
{
  "telefono": "+51987654321"
}
```

**Respuesta si existe**:
```json
{
  "data": {
    "solicitantePorTelefono": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "nombre": "Juan Pérez",
      "telefono": "+51987654321",
      "email": "juan@example.com"
    }
  }
}
```

**Respuesta si NO existe**:
```json
{
  "data": {
    "solicitantePorTelefono": null
  }
}
```

### 2. Verificar Estado de Incidente

**USO**: Después de crear un incidente, verificar su estado.

```graphql
query VerificarIncidente($id: UUID!) {
  incidente(id: $id) {
    id
    estado
    estadoValidacion
    prioridadFinal
    esVerosimil
    motivoRechazo
  }
}
```

### 3. Consultar Incidente Específico para Bot

```graphql
query ConsultarIncidenteBot($id: UUID!) {
  incidente(id: $id) {
    id
    descripcion
    estado
    prioridadFinal
    fechaReporte
    ubicacion {
      direccion
      distrito
    }
    solicitante {
      nombre
    }
  }
}
```

---

## Tipos de Datos

### Enums Principales

```graphql
enum EstadoIncidente {
  PENDIENTE
  APROBADO
  RECHAZADO
  EN_ATENCION
  ATENDIDO
  CANCELADO
}

enum EstadoValidacion {
  PENDIENTE_ANALISIS_ML
  ANALIZADO_ML
  VALIDADO_OPERADOR
  RECHAZADO
}

enum CanalOrigen {
  TELEFONO
  WEB
  MOBILE
  N8N
}

enum TipoDocumento {
  DNI
  CE
  PASAPORTE
}
```

### Tipos de Respuesta

```graphql
type Incidente {
  id: UUID!
  descripcion: String!
  prioridadInicial: Int!
  prioridadFinal: Int!
  estado: EstadoIncidente!
  estadoValidacion: EstadoValidacion!
  fechaReporte: DateTime!
  esVerosimil: Boolean
  motivoRechazo: String
  canalOrigen: CanalOrigen!
  solicitante: Solicitante!
  ubicacion: Ubicacion!
  multimedia: [Multimedia!]
  analisisMlTexto: AnalisisMlTexto
  analisisMlImagen: AnalisisMlImagen
}

type Solicitante {
  id: UUID!
  nombre: String!
  telefono: String!
  email: String
  tipoDocumento: TipoDocumento
  documento: String
  canalOrigen: CanalOrigen!
  fechaRegistro: DateTime!
}

type Ubicacion {
  id: UUID!
  latitud: BigDecimal!
  longitud: BigDecimal!
  direccion: String!
  distrito: String!
  referencia: String
  fechaCreacion: DateTime!
}
```

---

## Paginación

### Estructura de PageInput

```graphql
input PageInput {
  page: Int        # Número de página (0-indexed), default: 0
  size: Int        # Tamaño de página, default: 20
  orderBy: String  # Campo para ordenar, default: "fechaReporte"
  direction: Direction  # ASC o DESC, default: DESC
}

enum Direction {
  ASC
  DESC
}
```

### Estructura de Respuesta Paginada

```graphql
type IncidentePage {
  content: [Incidente!]!
  totalElements: Long!
  totalPages: Int!
  number: Int!
  size: Int!
  numberOfElements: Int!
  first: Boolean!
  last: Boolean!
  empty: Boolean!
}
```

---

## Ejemplos de Uso

### Desde Java (Microservicio de Despacho)

```java
@Service
@RequiredArgsConstructor
public class DespachoService {

    private final WebClient graphqlWebClient;

    public Flux<IncidenteDTO> obtenerIncidentesParaDespacho() {
        String query = """
            query {
              incidentesParaDespacho(paginacion: {
                page: 0
                size: 50
                orderBy: "prioridadFinal"
                direction: DESC
              }) {
                content {
                  id
                  descripcion
                  prioridadFinal
                  ubicacion {
                    latitud
                    longitud
                    direccion
                  }
                }
              }
            }
            """;

        return graphqlWebClient.post()
            .bodyValue(Map.of("query", query))
            .retrieve()
            .bodyToMono(GraphQLResponse.class)
            .flatMapMany(response ->
                Flux.fromIterable(response.getData().getIncidentesParaDespacho().getContent())
            );
    }
}
```

### Desde n8n (HTTP Request Node)

**Configuración del Nodo**:
- **Method**: POST
- **URL**: `http://ms-recepcion:8080/graphql`
- **Authentication**: None (o API Key si está configurado)
- **Send Body**: Yes
- **Body Content Type**: JSON

**Body**:
```json
{
  "query": "query VerificarSolicitante($telefono: String!) { solicitantePorTelefono(telefono: $telefono) { id nombre telefono } }",
  "variables": {
    "telefono": "{{$json[\"telefono\"]}}"
  }
}
```

**Procesamiento de Respuesta en n8n (Code Node)**:
```javascript
const response = $input.item.json;

if (response.data.solicitantePorTelefono) {
  // Solicitante existe
  return {
    solicitanteId: response.data.solicitantePorTelefono.id,
    existe: true,
    nombre: response.data.solicitantePorTelefono.nombre
  };
} else {
  // Solicitante NO existe, crear uno nuevo
  return {
    existe: false
  };
}
```

### Desde React (Frontend)

```typescript
import { useQuery, gql } from '@apollo/client';

const GET_INCIDENTES = gql`
  query GetIncidentes($estado: EstadoIncidente, $page: Int, $size: Int) {
    incidentesPorEstado(
      estado: $estado
      paginacion: { page: $page, size: $size }
    ) {
      content {
        id
        descripcion
        prioridadFinal
        estado
        fechaReporte
        solicitante {
          nombre
        }
      }
      totalElements
      totalPages
    }
  }
`;

function IncidentesList() {
  const { loading, error, data } = useQuery(GET_INCIDENTES, {
    variables: { estado: 'APROBADO', page: 0, size: 20 }
  });

  if (loading) return <p>Cargando...</p>;
  if (error) return <p>Error: {error.message}</p>;

  return (
    <div>
      {data.incidentesPorEstado.content.map(incidente => (
        <div key={incidente.id}>
          <h3>{incidente.descripcion}</h3>
          <p>Prioridad: {incidente.prioridadFinal}</p>
          <p>Solicitante: {incidente.solicitante.nombre}</p>
        </div>
      ))}
    </div>
  );
}
```

---

## Resumen de Queries por Microservicio

### Para Despacho de Ambulancias:
1. `incidentesParaDespacho` - Incidentes listos para despachar
2. `incidentesPrioridadAlta` - Solo urgencias
3. `incidente(id)` - Detalle completo de un incidente

### Para Frontend:
1. `incidentes` - Listar todos con filtros opcionales
2. `incidentesPorEstado` - Filtrar por estado
3. `incidentesPorPrioridad` - Filtrar por prioridad
4. `incidentesPendientesAnalisis` - Panel admin ML
5. `incidentesPorSolicitante` - Historial de solicitante
6. `incidentesPorRangoFechas` - Reportes
7. `solicitantes` - Gestión de solicitantes
8. `ubicaciones` - Gestión de ubicaciones

### Para n8n:
1. `solicitantePorTelefono` - Verificar si solicitante existe
2. `incidente(id)` - Verificar estado de incidente
3. `solicitante(id)` - Obtener datos de solicitante

---

## Notas Importantes

1. **UUIDs**: Todos los IDs son UUIDs en formato string: `"550e8400-e29b-41d4-a716-446655440000"`

2. **Fechas**: Usar formato ISO-8601: `"2025-11-07T10:30:00"` o `"2025-11-07T10:30:00Z"`

3. **Paginación**: Por defecto es page=0, size=20. Ajustar según necesidades.

4. **Errores**: GraphQL siempre retorna HTTP 200. Revisar el campo `errors` en la respuesta.

5. **Introspección**: Usar GraphiQL (`/graphiql`) para explorar el schema completo.

6. **Performance**: Evitar pedir campos innecesarios. GraphQL permite seleccionar exactamente lo que se necesita.

---

**Última actualización**: 2025-11-07
**Versión**: 1.0
**Microservicio**: ms_recepcion
