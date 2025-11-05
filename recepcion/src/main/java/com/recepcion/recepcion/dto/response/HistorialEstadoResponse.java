package com.recepcion.recepcion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoResponse {

    private UUID id;
    private String estadoAnterior;
    private String estadoNuevo;
    private String usuarioCambio;
    private String motivo;
    private Map<String, Object> metadata;
    private LocalDateTime fechaCambio;
}
