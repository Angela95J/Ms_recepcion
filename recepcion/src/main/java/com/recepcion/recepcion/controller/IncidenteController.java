package com.recepcion.recepcion.controller;

import com.recepcion.recepcion.dto.request.ActualizarEstadoIncidenteRequest;
import com.recepcion.recepcion.dto.request.ActualizarIncidenteRequest;
import com.recepcion.recepcion.dto.request.CrearIncidenteRequest;
import com.recepcion.recepcion.dto.response.IncidenteDetalleResponse;
import com.recepcion.recepcion.dto.response.IncidenteResponse;
import com.recepcion.recepcion.entity.EstadoIncidente;
import com.recepcion.recepcion.service.IncidenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para gestión de incidentes
 * Base URL: /api/incidentes
 */
@Slf4j
@RestController
@RequestMapping("/api/incidentes")
@RequiredArgsConstructor
public class IncidenteController {

    private final IncidenteService incidenteService;

    // ==================== CRUD BÁSICO ====================

    /**
     * Crear un nuevo incidente
     * POST /api/incidentes
     */
    @PostMapping
    public ResponseEntity<IncidenteResponse> crear(@Valid @RequestBody CrearIncidenteRequest request) {
        log.info("Creando nuevo incidente");
        IncidenteResponse response = incidenteService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener incidente por ID (vista básica)
     * GET /api/incidentes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<IncidenteResponse> buscarPorId(@PathVariable UUID id) {
        log.info("Buscando incidente por ID: {}", id);
        IncidenteResponse response = incidenteService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener incidente por ID con detalles completos (incluye relaciones)
     * GET /api/incidentes/{id}/detalle
     */
    @GetMapping("/{id}/detalle")
    public ResponseEntity<IncidenteDetalleResponse> buscarDetallePorId(@PathVariable UUID id) {
        log.info("Buscando detalle de incidente por ID: {}", id);
        IncidenteDetalleResponse response = incidenteService.buscarDetallePorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todos los incidentes con paginación
     * GET /api/incidentes
     */
    @GetMapping
    public ResponseEntity<Page<IncidenteResponse>> listarTodos(
            @PageableDefault(size = 20, sort = "fechaReporte") Pageable pageable) {
        log.info("Listando todos los incidentes - Página: {}, Tamaño: {}",
                 pageable.getPageNumber(), pageable.getPageSize());
        Page<IncidenteResponse> response = incidenteService.listarTodos(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar información del incidente
     * PUT /api/incidentes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<IncidenteResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarIncidenteRequest request) {
        log.info("Actualizando incidente ID: {}", id);
        IncidenteResponse response = incidenteService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar incidente
     * DELETE /api/incidentes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        log.info("Eliminando incidente ID: {}", id);
        incidenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== GESTIÓN DE ESTADOS ====================

    /**
     * Cambiar estado del incidente
     * PATCH /api/incidentes/{id}/estado
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<IncidenteResponse> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarEstadoIncidenteRequest request) {
        log.info("Cambiando estado del incidente ID: {} a {}", id, request.getNuevoEstado());
        IncidenteResponse response = incidenteService.cambiarEstado(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Aprobar incidente para despacho
     * POST /api/incidentes/{id}/aprobar
     */
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<IncidenteResponse> aprobar(@PathVariable UUID id) {
        log.info("Aprobando incidente ID: {}", id);
        IncidenteResponse response = incidenteService.aprobar(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Rechazar incidente
     * POST /api/incidentes/{id}/rechazar
     */
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<IncidenteResponse> rechazar(
            @PathVariable UUID id,
            @RequestParam String motivo) {
        log.info("Rechazando incidente ID: {} - Motivo: {}", id, motivo);
        IncidenteResponse response = incidenteService.rechazar(id, motivo);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancelar incidente
     * POST /api/incidentes/{id}/cancelar
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<IncidenteResponse> cancelar(
            @PathVariable UUID id,
            @RequestParam String motivo) {
        log.info("Cancelando incidente ID: {} - Motivo: {}", id, motivo);
        IncidenteResponse response = incidenteService.cancelar(id, motivo);
        return ResponseEntity.ok(response);
    }

    // ==================== CONSULTAS ESPECIALIZADAS ====================

    /**
     * Listar incidentes por estado
     * GET /api/incidentes/estado/{estado}
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Page<IncidenteResponse>> listarPorEstado(
            @PathVariable EstadoIncidente estado,
            @PageableDefault(size = 20, sort = "fechaReporte") Pageable pageable) {
        log.info("Listando incidentes por estado: {}", estado);
        Page<IncidenteResponse> response = incidenteService.listarPorEstado(estado, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar incidentes por prioridad
     * GET /api/incidentes/prioridad/{prioridad}
     */
    @GetMapping("/prioridad/{prioridad}")
    public ResponseEntity<List<IncidenteResponse>> listarPorPrioridad(@PathVariable Integer prioridad) {
        log.info("Listando incidentes por prioridad: {}", prioridad);
        List<IncidenteResponse> response = incidenteService.listarPorPrioridad(prioridad);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar incidentes listos para despacho
     * GET /api/incidentes/despacho
     */
    @GetMapping("/despacho")
    public ResponseEntity<List<IncidenteResponse>> listarParaDespacho() {
        log.info("Listando incidentes para despacho");
        List<IncidenteResponse> response = incidenteService.listarParaDespacho();
        return ResponseEntity.ok(response);
    }

    /**
     * Listar incidentes pendientes de análisis ML
     * GET /api/incidentes/pendientes-analisis
     */
    @GetMapping("/pendientes-analisis")
    public ResponseEntity<List<IncidenteResponse>> listarPendientesAnalisis() {
        log.info("Listando incidentes pendientes de análisis");
        List<IncidenteResponse> response = incidenteService.listarPendientesAnalisis();
        return ResponseEntity.ok(response);
    }

    /**
     * Listar incidentes de prioridad alta
     * GET /api/incidentes/prioridad-alta
     */
    @GetMapping("/prioridad-alta")
    public ResponseEntity<List<IncidenteResponse>> listarPrioridadAlta() {
        log.info("Listando incidentes de prioridad alta");
        List<IncidenteResponse> response = incidenteService.listarPrioridadAlta();
        return ResponseEntity.ok(response);
    }

    /**
     * Listar incidentes por solicitante
     * GET /api/incidentes/solicitante/{solicitanteId}
     */
    @GetMapping("/solicitante/{solicitanteId}")
    public ResponseEntity<List<IncidenteResponse>> listarPorSolicitante(@PathVariable UUID solicitanteId) {
        log.info("Listando incidentes del solicitante ID: {}", solicitanteId);
        List<IncidenteResponse> response = incidenteService.listarPorSolicitante(solicitanteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar incidentes por rango de fechas
     * GET /api/incidentes/rango-fechas?inicio={inicio}&fin={fin}
     */
    @GetMapping("/rango-fechas")
    public ResponseEntity<List<IncidenteResponse>> listarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.info("Listando incidentes por rango de fechas: {} - {}", inicio, fin);
        List<IncidenteResponse> response = incidenteService.listarPorRangoFechas(inicio, fin);
        return ResponseEntity.ok(response);
    }

    // ==================== ANÁLISIS ML ====================

    /**
     * Enviar incidente a análisis de texto
     * POST /api/incidentes/{id}/analisis-texto
     */
    @PostMapping("/{id}/analisis-texto")
    public ResponseEntity<Void> enviarAAnalisisTexto(@PathVariable UUID id) {
        log.info("Enviando incidente ID: {} a análisis de texto", id);
        incidenteService.enviarAAnalisisTexto(id);
        return ResponseEntity.accepted().build();
    }

    /**
     * Enviar incidente a análisis de imagen
     * POST /api/incidentes/{id}/analisis-imagen
     */
    @PostMapping("/{id}/analisis-imagen")
    public ResponseEntity<Void> enviarAAnalisisImagen(@PathVariable UUID id) {
        log.info("Enviando incidente ID: {} a análisis de imagen", id);
        incidenteService.enviarAAnalisisImagen(id);
        return ResponseEntity.accepted().build();
    }

    /**
     * Calcular prioridad final del incidente
     * POST /api/incidentes/{id}/calcular-prioridad
     */
    @PostMapping("/{id}/calcular-prioridad")
    public ResponseEntity<Void> calcularPrioridadFinal(@PathVariable UUID id) {
        log.info("Calculando prioridad final del incidente ID: {}", id);
        incidenteService.calcularPrioridadFinal(id);
        return ResponseEntity.ok().build();
    }

    // ==================== VALIDACIONES ====================

    /**
     * Verificar si un solicitante tiene incidentes activos
     * GET /api/incidentes/solicitante/{solicitanteId}/activos/existe
     */
    @GetMapping("/solicitante/{solicitanteId}/activos/existe")
    public ResponseEntity<Boolean> solicitanteTieneIncidentesActivos(@PathVariable UUID solicitanteId) {
        log.info("Verificando incidentes activos del solicitante ID: {}", solicitanteId);
        boolean tieneActivos = incidenteService.solicitanteTieneIncidentesActivos(solicitanteId);
        return ResponseEntity.ok(tieneActivos);
    }

    /**
     * Contar incidentes activos de un solicitante
     * GET /api/incidentes/solicitante/{solicitanteId}/activos/contar
     */
    @GetMapping("/solicitante/{solicitanteId}/activos/contar")
    public ResponseEntity<Long> contarIncidentesActivosPorSolicitante(@PathVariable UUID solicitanteId) {
        log.info("Contando incidentes activos del solicitante ID: {}", solicitanteId);
        long count = incidenteService.contarIncidentesActivosPorSolicitante(solicitanteId);
        return ResponseEntity.ok(count);
    }
}
