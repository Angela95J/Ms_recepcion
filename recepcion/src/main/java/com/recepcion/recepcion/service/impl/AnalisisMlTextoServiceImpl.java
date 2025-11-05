package com.recepcion.recepcion.service.impl;

import com.recepcion.recepcion.dto.response.AnalisisTextoResponse;
import com.recepcion.recepcion.entity.AnalisisMlTexto;
import com.recepcion.recepcion.entity.EstadoAnalisis;
import com.recepcion.recepcion.exception.ResourceNotFoundException;
import com.recepcion.recepcion.mapper.AnalisisTextoMapper;
import com.recepcion.recepcion.repository.AnalisisMlTextoRepository;
import com.recepcion.recepcion.service.AnalisisMlTextoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalisisMlTextoServiceImpl implements AnalisisMlTextoService {

    private final AnalisisMlTextoRepository analisisTextoRepository;
    private final AnalisisTextoMapper analisisTextoMapper;

    @Override
    public AnalisisTextoResponse buscarPorId(UUID id) {
        log.debug("Buscando análisis de texto por ID: {}", id);

        AnalisisMlTexto analisis = analisisTextoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AnalisisMlTexto", "id", id));

        return analisisTextoMapper.toResponse(analisis);
    }

    @Override
    public AnalisisTextoResponse buscarPorIncidente(UUID incidenteId) {
        log.debug("Buscando análisis de texto del incidente: {}", incidenteId);

        AnalisisMlTexto analisis = analisisTextoRepository.findByIncidenteId(incidenteId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró análisis de texto para el incidente: " + incidenteId));

        return analisisTextoMapper.toResponse(analisis);
    }

    @Override
    public List<AnalisisTextoResponse> listarTodos() {
        log.debug("Listando todos los análisis de texto");

        List<AnalisisMlTexto> analisis = analisisTextoRepository.findAll();
        return analisisTextoMapper.toResponseList(analisis);
    }

    @Override
    public List<AnalisisTextoResponse> listarPendientes() {
        log.debug("Listando análisis de texto pendientes");

        List<AnalisisMlTexto> analisis = analisisTextoRepository.findByEstadoAnalisis(EstadoAnalisis.PENDIENTE);
        return analisisTextoMapper.toResponseList(analisis);
    }

    @Override
    public List<AnalisisTextoResponse> listarPrioridadAlta() {
        log.debug("Listando análisis de texto con prioridad alta");

        List<AnalisisMlTexto> analisis = analisisTextoRepository.findAnalisisPrioridadAlta();
        return analisisTextoMapper.toResponseList(analisis);
    }
}
