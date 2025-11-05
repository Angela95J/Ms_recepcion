package com.recepcion.recepcion.controller;

import com.recepcion.recepcion.dto.request.SolicitanteRequest;
import com.recepcion.recepcion.dto.response.SolicitanteResponse;
import com.recepcion.recepcion.entity.CanalOrigen;
import com.recepcion.recepcion.service.SolicitanteService;
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
 * Controlador REST para gestión de solicitantes
 * Base URL: /api/solicitantes
 */
@Slf4j
@RestController
@RequestMapping("/api/solicitantes")
@RequiredArgsConstructor
public class SolicitanteController {

    private final SolicitanteService solicitanteService;

    /**
     * Crear un nuevo solicitante
     * POST /api/solicitantes
     */
    @PostMapping
    public ResponseEntity<SolicitanteResponse> crear(@Valid @RequestBody SolicitanteRequest request) {
        log.info("Creando nuevo solicitante con teléfono: {}", request.getTelefono());
        SolicitanteResponse response = solicitanteService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener solicitante por ID
     * GET /api/solicitantes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SolicitanteResponse> buscarPorId(@PathVariable UUID id) {
        log.info("Buscando solicitante por ID: {}", id);
        SolicitanteResponse response = solicitanteService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener solicitante por teléfono
     * GET /api/solicitantes/telefono/{telefono}
     */
    @GetMapping("/telefono/{telefono}")
    public ResponseEntity<SolicitanteResponse> buscarPorTelefono(@PathVariable String telefono) {
        log.info("Buscando solicitante por teléfono: {}", telefono);
        SolicitanteResponse response = solicitanteService.buscarPorTelefono(telefono);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todos los solicitantes con paginación
     * GET /api/solicitantes
     */
    @GetMapping
    public ResponseEntity<Page<SolicitanteResponse>> listarTodos(
            @PageableDefault(size = 20, sort = "fechaRegistro") Pageable pageable) {
        log.info("Listando todos los solicitantes - Página: {}, Tamaño: {}",
                 pageable.getPageNumber(), pageable.getPageSize());
        Page<SolicitanteResponse> response = solicitanteService.listarTodos(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar solicitantes por canal de origen
     * GET /api/solicitantes/canal/{canal}
     */
    @GetMapping("/canal/{canal}")
    public ResponseEntity<List<SolicitanteResponse>> listarPorCanalOrigen(@PathVariable CanalOrigen canal) {
        log.info("Listando solicitantes por canal: {}", canal);
        List<SolicitanteResponse> response = solicitanteService.listarPorCanalOrigen(canal);
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar solicitantes por nombre
     * GET /api/solicitantes/buscar?nombre={nombre}
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<SolicitanteResponse>> buscarPorNombre(@RequestParam String nombre) {
        log.info("Buscando solicitantes por nombre: {}", nombre);
        List<SolicitanteResponse> response = solicitanteService.buscarPorNombre(nombre);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar información del solicitante
     * PUT /api/solicitantes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SolicitanteResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody SolicitanteRequest request) {
        log.info("Actualizando solicitante ID: {}", id);
        SolicitanteResponse response = solicitanteService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar solicitante
     * DELETE /api/solicitantes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        log.info("Eliminando solicitante ID: {}", id);
        solicitanteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
