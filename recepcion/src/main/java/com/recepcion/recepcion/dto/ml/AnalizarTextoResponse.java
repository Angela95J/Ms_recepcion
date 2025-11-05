package com.recepcion.recepcion.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalizarTextoResponse {
    @JsonProperty("prioridad_calculada")
    private Integer prioridadCalculada;

    @JsonProperty("nivel_gravedad")
    private Integer nivelGravedad;

    @JsonProperty("tipo_incidente_predicho")
    private String tipoIncidentePredicho;

    @JsonProperty("categorias_detectadas")
    private Map<String, Object> categoriasDetectadas;

    @JsonProperty("palabras_clave_criticas")
    private List<String> palabrasClaveCriticas;

    @JsonProperty("entidades_medicas")
    private Map<String, Object> entidadesMedicas;

    @JsonProperty("score_confianza")
    private BigDecimal scoreConfianza;

    @JsonProperty("probabilidades_categorias")
    private Map<String, Object> probabilidadesCategorias;

    @JsonProperty("modelo_version")
    private String modeloVersion;

    @JsonProperty("algoritmo_usado")
    private String algoritmoUsado;

    @JsonProperty("tiempo_procesamiento_ms")
    private Integer tiempoProcesamientoMs;

    @JsonProperty("fecha_analisis")
    private LocalDateTime fechaAnalisis;
}
