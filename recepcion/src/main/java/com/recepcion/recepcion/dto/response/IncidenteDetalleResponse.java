package com.recepcion.recepcion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class IncidenteDetalleResponse extends IncidenteResponse {

    private AnalisisTextoResponse analisisTexto;
    private List<MultimediaResponse> multimedia;
    private List<HistorialEstadoResponse> historialEstados;
}
