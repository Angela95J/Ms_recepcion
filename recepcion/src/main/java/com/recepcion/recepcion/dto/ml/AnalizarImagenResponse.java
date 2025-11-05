package com.recepcion.recepcion.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalizarImagenResponse {
    @JsonProperty("es_imagen_accidente")
    private Boolean esImagenAccidente;

    @JsonProperty("score_veracidad")
    private BigDecimal scoreVeracidad;

    @JsonProperty("tipo_escena_detectada")
    private String tipoEscenaDetectada;

    @JsonProperty("nivel_gravedad_visual")
    private Integer nivelGravedadVisual;

    @JsonProperty("elementos_criticos_detectados")
    private Map<String, Object> elementosCriticosDetectados;

    @JsonProperty("objetos_detectados")
    private Map<String, Object> objetosDetectados;

    @JsonProperty("personas_detectadas")
    private Integer personasDetectadas;

    @JsonProperty("vehiculos_detectados")
    private Integer vehiculosDetectados;

    @JsonProperty("categorias_escena")
    private Map<String, Object> categoriasEscena;

    @JsonProperty("score_confianza_escena")
    private BigDecimal scoreConfianzaEscena;

    @JsonProperty("es_anomalia")
    private Boolean esAnomalia;

    @JsonProperty("score_anomalia")
    private BigDecimal scoreAnomalia;

    @JsonProperty("razon_sospecha")
    private String razonSospecha;

    @JsonProperty("calidad_imagen")
    private String calidadImagen;

    @JsonProperty("resolucion_imagen")
    private String resolucionImagen;

    @JsonProperty("es_imagen_clara")
    private Boolean esImagenClara;

    @JsonProperty("modelo_vision")
    private String modeloVision;

    @JsonProperty("modelo_veracidad")
    private String modeloVeracidad;

    @JsonProperty("tiempo_procesamiento_ms")
    private Integer tiempoProcesamientoMs;

    @JsonProperty("fecha_analisis")
    private LocalDateTime fechaAnalisis;
}
