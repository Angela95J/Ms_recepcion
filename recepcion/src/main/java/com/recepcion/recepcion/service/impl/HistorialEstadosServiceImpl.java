package com.recepcion.recepcion.service.impl;

import com.recepcion.recepcion.dto.response.HistorialEstadoResponse;
import com.recepcion.recepcion.entity.IncidenteHistorialEstados;
import com.recepcion.recepcion.exception.ResourceNotFoundException;
import com.recepcion.recepcion.mapper.HistorialEstadosMapper;
import com.recepcion.recepcion.repository.IncidenteHistorialEstadosRepository;
import com.recepcion.recepcion.service.HistorialEstadosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HistorialEstadosServiceImpl implements HistorialEstadosService {

    private final IncidenteHistorialEstadosRepository historialRepository;
    private final HistorialEstadosMapper historialMapper;

    @Override
    public List<HistorialEstadoResponse> buscarPorIncidente(UUID incidenteId) {
        log.debug("Buscando historial de estados del incidente: {}", incidenteId);

        List<IncidenteHistorialEstados> historial = historialRepository.findByIncidenteIdOrderByFechaCambioDesc(incidenteId);
        return historialMapper.toResponseList(historial);
    }

    @Override
    public List<HistorialEstadoResponse> buscarPorUsuario(String usuario) {
        log.debug("Buscando cambios realizados por usuario: {}", usuario);

        List<IncidenteHistorialEstados> historial = historialRepository.findByUsuarioCambio(usuario);
        return historialMapper.toResponseList(historial);
    }

    @Override
    public List<HistorialEstadoResponse> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Buscando historial entre {} y {}", fechaInicio, fechaFin);

        List<IncidenteHistorialEstados> historial = historialRepository.findByFechaCambioBetween(fechaInicio, fechaFin);
        return historialMapper.toResponseList(historial);
    }

    @Override
    public HistorialEstadoResponse obtenerUltimoCambio(UUID incidenteId) {
        log.debug("Obteniendo último cambio de estado del incidente: {}", incidenteId);

        IncidenteHistorialEstados ultimoCambio = historialRepository.findUltimoCambio(incidenteId);

        if (ultimoCambio == null) {
            throw new ResourceNotFoundException("No se encontró historial para el incidente: " + incidenteId);
        }

        return historialMapper.toResponse(ultimoCambio);
    }
}
