package com.recepcion.recepcion.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionRequest {

    @NotBlank(message = "La descripción textual es obligatoria")
    private String descripcionTextual;

    private String referencia;

    @DecimalMin(value = "-90.0", message = "Latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "Latitud debe estar entre -90 y 90")
    private BigDecimal latitud;

    @DecimalMin(value = "-180.0", message = "Longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "Longitud debe estar entre -180 y 180")
    private BigDecimal longitud;

    @Size(max = 100, message = "Ciudad no puede exceder 100 caracteres")
    private String ciudad;

    @Size(max = 100, message = "Distrito no puede exceder 100 caracteres")
    private String distrito;

    @Size(max = 100, message = "Zona no puede exceder 100 caracteres")
    private String zona;
}
