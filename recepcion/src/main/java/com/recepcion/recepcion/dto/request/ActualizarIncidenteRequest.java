package com.recepcion.recepcion.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarIncidenteRequest {

    private String descripcionOriginal;

    @Size(max = 100, message = "Tipo de incidente no puede exceder 100 caracteres")
    private String tipoIncidenteReportado;

    private String observaciones;
}
