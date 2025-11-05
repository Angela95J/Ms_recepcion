package com.recepcion.recepcion.service;

import com.recepcion.recepcion.dto.response.AnalisisTextoResponse;

import java.util.List;
import java.util.UUID;

public interface AnalisisMlTextoService {

    /**
     * Buscar análisis por ID
     */
    AnalisisTextoResponse buscarPorId(UUID id);

    /**
     * Buscar análisis por incidente
     */
    AnalisisTextoResponse buscarPorIncidente(UUID incidenteId);

    /**
     * Listar todos los análisis
     */
    List<AnalisisTextoResponse> listarTodos();

    /**
     * Listar análisis pendientes
     */
    List<AnalisisTextoResponse> listarPendientes();

    /**
     * Listar análisis con prioridad alta
     */
    List<AnalisisTextoResponse> listarPrioridadAlta();
}
