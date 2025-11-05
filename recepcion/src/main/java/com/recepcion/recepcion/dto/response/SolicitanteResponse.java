package com.recepcion.recepcion.dto.response;

import com.recepcion.recepcion.entity.CanalOrigen;
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
public class SolicitanteResponse {

    private UUID id;
    private String nombreCompleto;
    private String telefono;
    private CanalOrigen canalOrigen;
    private LocalDateTime fechaRegistro;
}
