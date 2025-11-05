package com.recepcion.recepcion.dto.response;

import com.recepcion.recepcion.entity.EstadoIncidente;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class IncidenteResponse {

    private UUID id;
    private SolicitanteResponse solicitante;
    private UbicacionResponse ubicacion;
    private String descripcionOriginal;
    private String tipoIncidenteReportado;
    private String tipoIncidenteClasificado;
    private Integer prioridadInicial;
    private Integer prioridadTexto;
    private Integer prioridadImagen;
    private Integer prioridadFinal;
    private BigDecimal scoreVeracidad;
    private Boolean esVerosimil;
    private EstadoIncidente estadoIncidente;
    private String motivoRechazo;
    private LocalDateTime fechaReporte;
    private LocalDateTime fechaAnalisisCompletado;
    private LocalDateTime fechaUltimaActualizacion;
    private String observaciones;
}
