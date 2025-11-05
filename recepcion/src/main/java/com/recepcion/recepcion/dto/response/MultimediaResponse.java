package com.recepcion.recepcion.dto.response;

import com.recepcion.recepcion.entity.TipoArchivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultimediaResponse {

    private UUID id;
    private UUID incidenteId;
    private String urlArchivo;
    private String urlMiniatura;
    private String nombreArchivo;
    private TipoArchivo tipoArchivo;
    private String formatoArchivo;
    private Long tamanoBytes;
    private String descripcion;
    private Boolean esPrincipal;
    private Boolean requiereAnalisisMl;
    private Boolean analisisCompletado;
    private LocalDateTime fechaSubida;
}
