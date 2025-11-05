package com.recepcion.recepcion.controller;

import com.recepcion.recepcion.dto.response.AnalisisImagenResponse;
import com.recepcion.recepcion.service.AnalisisMlImagenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para consultar análisis de imágenes ML
 * Base URL: /api/analisis-imagen
 */
@Slf4j
@RestController
@RequestMapping("/api/analisis-imagen")
@RequiredArgsConstructor
public class AnalisisImagenController {

    private final AnalisisMlImagenService analisisMlImagenService;

    /**
     * Obtener análisis de imagen por ID
     * GET /api/analisis-imagen/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnalisisImagenResponse> buscarPorId(@PathVariable UUID id) {
        log.info("Buscando análisis de imagen por ID: {}", id);
        AnalisisImagenResponse response = analisisMlImagenService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener análisis de imagen por multimedia
     * GET /api/analisis-imagen/multimedia/{multimediaId}
     */
    @GetMapping("/multimedia/{multimediaId}")
    public ResponseEntity<AnalisisImagenResponse> buscarPorMultimedia(@PathVariable UUID multimediaId) {
        log.info("Buscando análisis de imagen para multimedia ID: {}", multimediaId);
        AnalisisImagenResponse response = analisisMlImagenService.buscarPorMultimedia(multimediaId);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar análisis de imágenes de un incidente
     * GET /api/analisis-imagen/incidente/{incidenteId}
     */
    @GetMapping("/incidente/{incidenteId}")
    public ResponseEntity<List<AnalisisImagenResponse>> listarPorIncidente(@PathVariable UUID incidenteId) {
        log.info("Listando análisis de imagen del incidente ID: {}", incidenteId);
        List<AnalisisImagenResponse> response = analisisMlImagenService.listarPorIncidente(incidenteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todos los análisis de imagen
     * GET /api/analisis-imagen
     */
    @GetMapping
    public ResponseEntity<List<AnalisisImagenResponse>> listarTodos() {
        log.info("Listando todos los análisis de imagen");
        List<AnalisisImagenResponse> response = analisisMlImagenService.listarTodos();
        return ResponseEntity.ok(response);
    }

    /**
     * Listar análisis pendientes
     * GET /api/analisis-imagen/pendientes
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<AnalisisImagenResponse>> listarPendientes() {
        log.info("Listando análisis de imagen pendientes");
        List<AnalisisImagenResponse> response = analisisMlImagenService.listarPendientes();
        return ResponseEntity.ok(response);
    }

    /**
     * Listar análisis con veracidad baja
     * GET /api/analisis-imagen/veracidad-baja
     */
    @GetMapping("/veracidad-baja")
    public ResponseEntity<List<AnalisisImagenResponse>> listarConVeracidadBaja() {
        log.info("Listando análisis de imagen con veracidad baja");
        List<AnalisisImagenResponse> response = analisisMlImagenService.listarConVeracidadBaja();
        return ResponseEntity.ok(response);
    }

    /**
     * Listar análisis marcados como anomalías
     * GET /api/analisis-imagen/anomalias
     */
    @GetMapping("/anomalias")
    public ResponseEntity<List<AnalisisImagenResponse>> listarAnomalias() {
        log.info("Listando análisis de imagen con anomalías");
        List<AnalisisImagenResponse> response = analisisMlImagenService.listarAnomalias();
        return ResponseEntity.ok(response);
    }
}
