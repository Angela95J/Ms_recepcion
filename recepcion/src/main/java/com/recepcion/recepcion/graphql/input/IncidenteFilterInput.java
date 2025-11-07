package com.recepcion.recepcion.graphql.input;

import com.recepcion.recepcion.entity.CanalOrigen;
import com.recepcion.recepcion.entity.EstadoIncidente;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Input para filtros de búsqueda de incidentes en GraphQL
 */
@Data
public class IncidenteFilterInput {
    private EstadoIncidente estado;
    private Integer prioridadMin;
    private Integer prioridadMax;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private UUID solicitanteId;
    private String distrito;
    private Boolean esVerosimil;
    private CanalOrigen canalOrigen;
}
