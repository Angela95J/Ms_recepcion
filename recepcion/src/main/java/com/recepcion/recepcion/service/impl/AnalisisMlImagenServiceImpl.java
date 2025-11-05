package com.recepcion.recepcion.service.impl;

import com.recepcion.recepcion.dto.response.AnalisisImagenResponse;
import com.recepcion.recepcion.entity.AnalisisMlImagen;
import com.recepcion.recepcion.entity.EstadoAnalisis;
import com.recepcion.recepcion.exception.ResourceNotFoundException;
import com.recepcion.recepcion.mapper.AnalisisImagenMapper;
import com.recepcion.recepcion.repository.AnalisisMlImagenRepository;
import com.recepcion.recepcion.service.AnalisisMlImagenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalisisMlImagenServiceImpl implements AnalisisMlImagenService {

    private final AnalisisMlImagenRepository analisisImagenRepository;
    private final AnalisisImagenMapper analisisImagenMapper;

    @Override
    public AnalisisImagenResponse buscarPorId(UUID id) {
        log.debug("Buscando análisis de imagen por ID: {}", id);

        AnalisisMlImagen analisis = analisisImagenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AnalisisMlImagen", "id", id));

        return analisisImagenMapper.toResponse(analisis);
    }

    @Override
    public AnalisisImagenResponse buscarPorMultimedia(UUID multimediaId) {
        log.debug("Buscando análisis de imagen para multimedia: {}", multimediaId);

        AnalisisMlImagen analisis = analisisImagenRepository.findByMultimediaId(multimediaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró análisis de imagen para el multimedia: " + multimediaId));

        return analisisImagenMapper.toResponse(analisis);
    }

    @Override
    public List<AnalisisImagenResponse> listarPorIncidente(UUID incidenteId) {
        log.debug("Listando análisis de imagen del incidente: {}", incidenteId);

        List<AnalisisMlImagen> analisis = analisisImagenRepository.findByIncidenteId(incidenteId);
        return analisisImagenMapper.toResponseList(analisis);
    }

    @Override
    public List<AnalisisImagenResponse> listarTodos() {
        log.debug("Listando todos los análisis de imagen");

        List<AnalisisMlImagen> analisis = analisisImagenRepository.findAll();
        return analisisImagenMapper.toResponseList(analisis);
    }

    @Override
    public List<AnalisisImagenResponse> listarPendientes() {
        log.debug("Listando análisis de imagen pendientes");

        List<AnalisisMlImagen> analisis = analisisImagenRepository.findByEstadoAnalisis(EstadoAnalisis.PENDIENTE);
        return analisisImagenMapper.toResponseList(analisis);
    }

    @Override
    public List<AnalisisImagenResponse> listarConVeracidadBaja() {
        log.debug("Listando análisis con veracidad baja (< 0.5)");

        BigDecimal umbral = new BigDecimal("0.5");
        List<AnalisisMlImagen> analisis = analisisImagenRepository.findConVeracidadBaja(umbral);
        return analisisImagenMapper.toResponseList(analisis);
    }

    @Override
    public List<AnalisisImagenResponse> listarAnomalias() {
        log.debug("Listando análisis marcados como anomalías");

        List<AnalisisMlImagen> analisis = analisisImagenRepository.findByEsAnomaliaTrue();
        return analisisImagenMapper.toResponseList(analisis);
    }
}
