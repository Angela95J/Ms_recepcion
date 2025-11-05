package com.recepcion.recepcion.dto.request;

import com.recepcion.recepcion.entity.EstadoIncidente;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarEstadoIncidenteRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoIncidente nuevoEstado;

    private String motivoRechazo;

    private String observaciones;
}
