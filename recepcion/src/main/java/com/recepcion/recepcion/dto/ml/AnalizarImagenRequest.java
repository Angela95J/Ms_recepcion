package com.recepcion.recepcion.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalizarImagenRequest {
    @JsonProperty("imagen_path")
    private String imagenPath;

    @JsonProperty("multimedia_id")
    private UUID multimediaId;

    @JsonProperty("incidente_id")
    private UUID incidenteId;
}
