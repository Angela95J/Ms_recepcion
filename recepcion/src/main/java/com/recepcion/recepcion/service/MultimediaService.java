package com.recepcion.recepcion.service;

import com.recepcion.recepcion.dto.response.MultimediaResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MultimediaService {

    /**
     * Subir archivo multimedia a un incidente
     */
    MultimediaResponse subirArchivo(UUID incidenteId, MultipartFile archivo, String descripcion, Boolean esPrincipal);

    /**
     * Buscar multimedia por ID
     */
    MultimediaResponse buscarPorId(UUID id);

    /**
     * Listar multimedia de un incidente
     */
    List<MultimediaResponse> listarPorIncidente(UUID incidenteId);

    /**
     * Descargar archivo multimedia
     */
    byte[] descargarArchivo(UUID id);

    /**
     * Eliminar archivo multimedia
     */
    void eliminar(UUID id);

    /**
     * Listar multimedia pendiente de análisis ML
     */
    List<MultimediaResponse> listarPendientesAnalisis();

    /**
     * Marcar multimedia como analizado
     */
    void marcarComoAnalizado(UUID id);
}
