package com.recepcion.recepcion.controller;

import com.recepcion.recepcion.dto.request.UbicacionRequest;
import com.recepcion.recepcion.dto.response.UbicacionResponse;
import com.recepcion.recepcion.service.UbicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para gestión de ubicaciones
 * Base URL: /api/ubicaciones
 */
@Slf4j
@RestController
@RequestMapping("/ubicaciones")
@RequiredArgsConstructor
public class UbicacionController {

    private final UbicacionService ubicacionService;

    /**
     * Crear una nueva ubicación
     * POST /api/ubicaciones
     */
    @PostMapping
    public ResponseEntity<UbicacionResponse> crear(@Valid @RequestBody UbicacionRequest request) {
        log.info("Creando nueva ubicación");
        UbicacionResponse response = ubicacionService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener ubicación por ID
     * GET /api/ubicaciones/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UbicacionResponse> buscarPorId(@PathVariable UUID id) {
        log.info("Buscando ubicación por ID: {}", id);
        UbicacionResponse response = ubicacionService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todas las ubicaciones con paginación
     * GET /api/ubicaciones
     */
    @GetMapping
    public ResponseEntity<Page<UbicacionResponse>> listarTodos(
            @PageableDefault(size = 20, sort = "ciudad") Pageable pageable) {
        log.info("Listando todas las ubicaciones - Página: {}, Tamaño: {}",
                 pageable.getPageNumber(), pageable.getPageSize());
        Page<UbicacionResponse> response = ubicacionService.listarTodos(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar ubicaciones por distrito
     * GET /api/ubicaciones/distrito/{distrito}
     */
    @GetMapping("/distrito/{distrito}")
    public ResponseEntity<List<UbicacionResponse>> listarPorDistrito(@PathVariable String distrito) {
        log.info("Listando ubicaciones por distrito: {}", distrito);
        List<UbicacionResponse> response = ubicacionService.listarPorDistrito(distrito);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar información de la ubicación
     * PUT /api/ubicaciones/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UbicacionResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody UbicacionRequest request) {
        log.info("Actualizando ubicación ID: {}", id);
        UbicacionResponse response = ubicacionService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar ubicación
     * DELETE /api/ubicaciones/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        log.info("Eliminando ubicación ID: {}", id);
        ubicacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
