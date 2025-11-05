package com.recepcion.recepcion.service;

import com.recepcion.recepcion.dto.response.AnalisisImagenResponse;

import java.util.List;
import java.util.UUID;

public interface AnalisisMlImagenService {

    /**
     * Buscar análisis por ID
     */
    AnalisisImagenResponse buscarPorId(UUID id);

    /**
     * Buscar análisis por multimedia
     */
    AnalisisImagenResponse buscarPorMultimedia(UUID multimediaId);

    /**
     * Listar análisis de un incidente
     */
    List<AnalisisImagenResponse> listarPorIncidente(UUID incidenteId);

    /**
     * Listar todos los análisis
     */
    List<AnalisisImagenResponse> listarTodos();

    /**
     * Listar análisis pendientes
     */
    List<AnalisisImagenResponse> listarPendientes();

    /**
     * Listar análisis con veracidad baja (posibles falsas alarmas)
     */
    List<AnalisisImagenResponse> listarConVeracidadBaja();

    /**
     * Listar análisis marcados como anomalías
     */
    List<AnalisisImagenResponse> listarAnomalias();
}
