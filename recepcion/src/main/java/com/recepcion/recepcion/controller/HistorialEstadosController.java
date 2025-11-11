package com.recepcion.recepcion.controller;

import com.recepcion.recepcion.dto.response.HistorialEstadoResponse;
import com.recepcion.recepcion.service.HistorialEstadosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para consultar historial de cambios de estado
 * Base URL: /api/historial-estados
 */
@Slf4j
@RestController
@RequestMapping("/historial-estados")
@RequiredArgsConstructor
public class HistorialEstadosController {

    private final HistorialEstadosService historialEstadosService;

    /**
     * Obtener historial de un incidente
     * GET /api/historial-estados/incidente/{incidenteId}
     */
    @GetMapping("/incidente/{incidenteId}")
    public ResponseEntity<List<HistorialEstadoResponse>> buscarPorIncidente(@PathVariable UUID incidenteId) {
        log.info("Buscando historial de cambios del incidente ID: {}", incidenteId);
        List<HistorialEstadoResponse> response = historialEstadosService.buscarPorIncidente(incidenteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener último cambio de estado de un incidente
     * GET /api/historial-estados/incidente/{incidenteId}/ultimo
     */
    @GetMapping("/incidente/{incidenteId}/ultimo")
    public ResponseEntity<HistorialEstadoResponse> obtenerUltimoCambio(@PathVariable UUID incidenteId) {
        log.info("Obteniendo último cambio de estado del incidente ID: {}", incidenteId);
        HistorialEstadoResponse response = historialEstadosService.obtenerUltimoCambio(incidenteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar cambios por usuario
     * GET /api/historial-estados/usuario/{usuario}
     */
    @GetMapping("/usuario/{usuario}")
    public ResponseEntity<List<HistorialEstadoResponse>> buscarPorUsuario(@PathVariable String usuario) {
        log.info("Buscando cambios realizados por el usuario: {}", usuario);
        List<HistorialEstadoResponse> response = historialEstadosService.buscarPorUsuario(usuario);
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar cambios por rango de fechas
     * GET /api/historial-estados/rango-fechas?inicio={inicio}&fin={fin}
     */
    @GetMapping("/rango-fechas")
    public ResponseEntity<List<HistorialEstadoResponse>> buscarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.info("Buscando cambios de estado en el rango: {} - {}", inicio, fin);
        List<HistorialEstadoResponse> response = historialEstadosService.buscarPorRangoFechas(inicio, fin);
        return ResponseEntity.ok(response);
    }
}
