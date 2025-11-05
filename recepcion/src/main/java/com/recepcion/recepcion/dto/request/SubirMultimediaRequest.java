package com.recepcion.recepcion.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubirMultimediaRequest {

    @NotNull(message = "El ID del incidente es obligatorio")
    private UUID incidenteId;

    @NotNull(message = "El archivo es obligatorio")
    private MultipartFile archivo;

    private String descripcion;

    private Boolean esPrincipal;
}
