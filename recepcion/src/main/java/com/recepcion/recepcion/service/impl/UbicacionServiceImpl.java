package com.recepcion.recepcion.service.impl;

import com.recepcion.recepcion.dto.request.UbicacionRequest;
import com.recepcion.recepcion.dto.response.UbicacionResponse;
import com.recepcion.recepcion.entity.Ubicacion;
import com.recepcion.recepcion.exception.ResourceNotFoundException;
import com.recepcion.recepcion.mapper.UbicacionMapper;
import com.recepcion.recepcion.repository.UbicacionRepository;
import com.recepcion.recepcion.service.UbicacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UbicacionServiceImpl implements UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final UbicacionMapper ubicacionMapper;

    @Override
    public UbicacionResponse crear(UbicacionRequest request) {
        log.info("Creando nueva ubicación");

        Ubicacion ubicacion = ubicacionMapper.toEntity(request);
        Ubicacion guardada = ubicacionRepository.save(ubicacion);

        log.info("Ubicación creada exitosamente con ID: {}", guardada.getId());
        return ubicacionMapper.toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public UbicacionResponse buscarPorId(UUID id) {
        log.debug("Buscando ubicación por ID: {}", id);

        Ubicacion ubicacion = ubicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ubicacion", "id", id));

        return ubicacionMapper.toResponse(ubicacion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UbicacionResponse> listarTodos(Pageable pageable) {
        log.debug("Listando todas las ubicaciones - Página: {}", pageable.getPageNumber());

        return ubicacionRepository.findAll(pageable)
                .map(ubicacionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionResponse> listarPorDistrito(String distrito) {
        log.debug("Listando ubicaciones por distrito: {}", distrito);

        List<Ubicacion> ubicaciones = ubicacionRepository.findByDistrito(distrito);
        return ubicacionMapper.toResponseList(ubicaciones);
    }

    @Override
    public UbicacionResponse actualizar(UUID id, UbicacionRequest request) {
        log.info("Actualizando ubicación ID: {}", id);

        Ubicacion ubicacion = ubicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ubicacion", "id", id));

        ubicacionMapper.updateEntityFromRequest(request, ubicacion);
        Ubicacion actualizada = ubicacionRepository.save(ubicacion);

        log.info("Ubicación actualizada exitosamente: {}", id);
        return ubicacionMapper.toResponse(actualizada);
    }

    @Override
    public void eliminar(UUID id) {
        log.info("Eliminando ubicación ID: {}", id);

        if (!ubicacionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ubicacion", "id", id);
        }

        ubicacionRepository.deleteById(id);
        log.info("Ubicación eliminada exitosamente: {}", id);
    }
}
