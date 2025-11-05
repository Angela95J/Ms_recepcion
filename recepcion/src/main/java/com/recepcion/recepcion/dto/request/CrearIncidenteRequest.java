package com.recepcion.recepcion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearIncidenteRequest {

    @NotNull(message = "Los datos del solicitante son obligatorios")
    @Valid
    private SolicitanteRequest solicitante;

    @NotNull(message = "Los datos de ubicación son obligatorios")
    @Valid
    private UbicacionRequest ubicacion;

    @NotBlank(message = "La descripción del incidente es obligatoria")
    private String descripcionOriginal;

    @Size(max = 100, message = "Tipo de incidente no puede exceder 100 caracteres")
    private String tipoIncidenteReportado;
}
