package com.recepcion.recepcion.dto.response;

import com.recepcion.recepcion.entity.CalidadImagen;
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
public class AnalisisImagenResponse {

    private UUID id;
    private UUID multimediaId;
    private Boolean esImagenAccidente;
    private BigDecimal scoreVeracidad;
    private String tipoEscenaDetectada;
    private Integer nivelGravedadVisual;
    private Map<String, Object> elementosCriticosDetectados;
    private Map<String, Object> objetosDetectados;
    private Integer personasDetectadas;
    private Integer vehiculosDetectados;
    private Map<String, Object> categoriasEscena;
    private BigDecimal scoreConfianzaEscena;
    private Boolean esAnomalia;
    private BigDecimal scoreAnomalia;
    private String razonSospecha;
    private CalidadImagen calidadImagen;
    private String resolucionImagen;
    private Boolean esImagenClara;
    private String modeloVision;
    private String modeloVeracidad;
    private Integer tiempoProcesamientoMs;
    private LocalDateTime fechaAnalisis;
    private EstadoAnalisis estadoAnalisis;
    private String errorMensaje;
}
