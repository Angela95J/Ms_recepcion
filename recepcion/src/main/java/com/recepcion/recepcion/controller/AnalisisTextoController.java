package com.recepcion.recepcion.controller;

import com.recepcion.recepcion.dto.response.AnalisisTextoResponse;
import com.recepcion.recepcion.service.AnalisisMlTextoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para consultar análisis de texto ML
 * Base URL: /api/analisis-texto
 */
@Slf4j
@RestController
@RequestMapping("/analisis-texto")
@RequiredArgsConstructor
public class AnalisisTextoController {

    private final AnalisisMlTextoService analisisMlTextoService;

    /**
     * Obtener análisis de texto por ID
     * GET /api/analisis-texto/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnalisisTextoResponse> buscarPorId(@PathVariable UUID id) {
        log.info("Buscando análisis de texto por ID: {}", id);
        AnalisisTextoResponse response = analisisMlTextoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener análisis de texto por incidente
     * GET /api/analisis-texto/incidente/{incidenteId}
     */
    @GetMapping("/incidente/{incidenteId}")
    public ResponseEntity<AnalisisTextoResponse> buscarPorIncidente(@PathVariable UUID incidenteId) {
        log.info("Buscando análisis de texto del incidente ID: {}", incidenteId);
        AnalisisTextoResponse response = analisisMlTextoService.buscarPorIncidente(incidenteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todos los análisis de texto
     * GET /api/analisis-texto
     */
    @GetMapping
    public ResponseEntity<List<AnalisisTextoResponse>> listarTodos() {
        log.info("Listando todos los análisis de texto");
        List<AnalisisTextoResponse> response = analisisMlTextoService.listarTodos();
        return ResponseEntity.ok(response);
    }

    /**
     * Listar análisis pendientes
     * GET /api/analisis-texto/pendientes
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<AnalisisTextoResponse>> listarPendientes() {
        log.info("Listando análisis de texto pendientes");
        List<AnalisisTextoResponse> response = analisisMlTextoService.listarPendientes();
        return ResponseEntity.ok(response);
    }

    /**
     * Listar análisis con prioridad alta
     * GET /api/analisis-texto/prioridad-alta
     */
    @GetMapping("/prioridad-alta")
    public ResponseEntity<List<AnalisisTextoResponse>> listarPrioridadAlta() {
        log.info("Listando análisis de texto con prioridad alta");
        List<AnalisisTextoResponse> response = analisisMlTextoService.listarPrioridadAlta();
        return ResponseEntity.ok(response);
    }
}
