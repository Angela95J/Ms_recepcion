package com.recepcion.recepcion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionResponse {

    private UUID id;
    private String descripcionTextual;
    private String referencia;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String ciudad;
    private String distrito;
    private String zona;
    private LocalDateTime fechaCreacion;
}
