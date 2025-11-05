package com.recepcion.recepcion.service;

import com.recepcion.recepcion.dto.request.UbicacionRequest;
import com.recepcion.recepcion.dto.response.UbicacionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UbicacionService {

    /**
     * Crear una nueva ubicación
     */
    UbicacionResponse crear(UbicacionRequest request);

    /**
     * Buscar ubicación por ID
     */
    UbicacionResponse buscarPorId(UUID id);

    /**
     * Listar todas las ubicaciones con paginación
     */
    Page<UbicacionResponse> listarTodos(Pageable pageable);

    /**
     * Listar ubicaciones por distrito
     */
    List<UbicacionResponse> listarPorDistrito(String distrito);

    /**
     * Actualizar ubicación
     */
    UbicacionResponse actualizar(UUID id, UbicacionRequest request);

    /**
     * Eliminar ubicación
     */
    void eliminar(UUID id);
}
