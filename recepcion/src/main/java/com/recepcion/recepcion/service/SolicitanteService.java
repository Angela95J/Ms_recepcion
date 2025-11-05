package com.recepcion.recepcion.service;

import com.recepcion.recepcion.dto.request.SolicitanteRequest;
import com.recepcion.recepcion.dto.response.SolicitanteResponse;
import com.recepcion.recepcion.entity.CanalOrigen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SolicitanteService {

    /**
     * Crear un nuevo solicitante
     */
    SolicitanteResponse crear(SolicitanteRequest request);

    /**
     * Buscar solicitante por ID
     */
    SolicitanteResponse buscarPorId(UUID id);

    /**
     * Buscar solicitante por teléfono
     */
    SolicitanteResponse buscarPorTelefono(String telefono);

    /**
     * Listar todos los solicitantes con paginación
     */
    Page<SolicitanteResponse> listarTodos(Pageable pageable);

    /**
     * Listar solicitantes por canal de origen
     */
    List<SolicitanteResponse> listarPorCanalOrigen(CanalOrigen canal);

    /**
     * Actualizar solicitante
     */
    SolicitanteResponse actualizar(UUID id, SolicitanteRequest request);

    /**
     * Eliminar solicitante (solo ADMIN)
     */
    void eliminar(UUID id);

    /**
     * Buscar solicitante por nombre (búsqueda parcial)
     */
    List<SolicitanteResponse> buscarPorNombre(String nombre);
}
