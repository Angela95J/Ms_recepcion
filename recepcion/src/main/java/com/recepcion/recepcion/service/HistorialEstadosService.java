package com.recepcion.recepcion.service;

import com.recepcion.recepcion.dto.response.HistorialEstadoResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface HistorialEstadosService {

    /**
     * Buscar historial de un incidente (ordenado por fecha descendente)
     */
    List<HistorialEstadoResponse> buscarPorIncidente(UUID incidenteId);

    /**
     * Buscar cambios por usuario
     */
    List<HistorialEstadoResponse> buscarPorUsuario(String usuario);

    /**
     * Buscar cambios por rango de fechas
     */
    List<HistorialEstadoResponse> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtener último cambio de estado de un incidente
     */
    HistorialEstadoResponse obtenerUltimoCambio(UUID incidenteId);
}
