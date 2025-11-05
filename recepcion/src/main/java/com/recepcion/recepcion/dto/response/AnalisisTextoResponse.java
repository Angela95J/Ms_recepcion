package com.recepcion.recepcion.dto.response;

import com.recepcion.recepcion.entity.EstadoAnalisis;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalisisTextoResponse {

    private UUID id;
    private String textoAnalizado;
    private Integer prioridadCalculada;
    private Integer nivelGravedad;
    private String tipoIncidentePredicho;
    private Map<String, Object> categoriasDetectadas;
    private Map<String, Object> palabrasClaveCriticas;
    private Map<String, Object> entidadesMedicas;
    private BigDecimal scoreConfianza;
    private Map<String, Object> probabilidadesCategorias;
    private String modeloVersion;
    private String algoritmoUsado;
    private Integer tiempoProcesamientoMs;
    private LocalDateTime fechaAnalisis;
    private EstadoAnalisis estadoAnalisis;
    private String errorMensaje;
}
