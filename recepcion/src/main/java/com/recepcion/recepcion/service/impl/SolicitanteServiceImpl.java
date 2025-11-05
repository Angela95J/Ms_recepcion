package com.recepcion.recepcion.service.impl;

import com.recepcion.recepcion.dto.request.SolicitanteRequest;
import com.recepcion.recepcion.dto.response.SolicitanteResponse;
import com.recepcion.recepcion.entity.CanalOrigen;
import com.recepcion.recepcion.entity.Solicitante;
import com.recepcion.recepcion.exception.ConflictException;
import com.recepcion.recepcion.exception.ResourceNotFoundException;
import com.recepcion.recepcion.mapper.SolicitanteMapper;
import com.recepcion.recepcion.repository.SolicitanteRepository;
import com.recepcion.recepcion.service.SolicitanteService;
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
public class SolicitanteServiceImpl implements SolicitanteService {

    private final SolicitanteRepository solicitanteRepository;
    private final SolicitanteMapper solicitanteMapper;

    @Override
    public SolicitanteResponse crear(SolicitanteRequest request) {
        log.info("Creando nuevo solicitante con teléfono: {}", request.getTelefono());

        // Validar que el teléfono no esté registrado
        if (solicitanteRepository.existsByTelefono(request.getTelefono())) {
            throw new ConflictException("Ya existe un solicitante con el teléfono: " + request.getTelefono());
        }

        Solicitante solicitante = solicitanteMapper.toEntity(request);
        Solicitante guardado = solicitanteRepository.save(solicitante);

        log.info("Solicitante creado exitosamente con ID: {}", guardado.getId());
        return solicitanteMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitanteResponse buscarPorId(UUID id) {
        log.debug("Buscando solicitante por ID: {}", id);

        Solicitante solicitante = solicitanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitante", "id", id));

        return solicitanteMapper.toResponse(solicitante);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitanteResponse buscarPorTelefono(String telefono) {
        log.debug("Buscando solicitante por teléfono: {}", telefono);

        Solicitante solicitante = solicitanteRepository.findByTelefono(telefono)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitante", "teléfono", telefono));

        return solicitanteMapper.toResponse(solicitante);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SolicitanteResponse> listarTodos(Pageable pageable) {
        log.debug("Listando todos los solicitantes - Página: {}", pageable.getPageNumber());

        return solicitanteRepository.findAll(pageable)
                .map(solicitanteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitanteResponse> listarPorCanalOrigen(CanalOrigen canal) {
        log.debug("Listando solicitantes por canal: {}", canal);

        List<Solicitante> solicitantes = solicitanteRepository.findByCanalOrigen(canal);
        return solicitanteMapper.toResponseList(solicitantes);
    }

    @Override
    public SolicitanteResponse actualizar(UUID id, SolicitanteRequest request) {
        log.info("Actualizando solicitante ID: {}", id);

        Solicitante solicitante = solicitanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitante", "id", id));

        // Validar teléfono si cambió
        if (!solicitante.getTelefono().equals(request.getTelefono()) &&
            solicitanteRepository.existsByTelefono(request.getTelefono())) {
            throw new ConflictException("Ya existe un solicitante con el teléfono: " + request.getTelefono());
        }

        solicitanteMapper.updateEntityFromRequest(request, solicitante);
        Solicitante actualizado = solicitanteRepository.save(solicitante);

        log.info("Solicitante actualizado exitosamente: {}", id);
        return solicitanteMapper.toResponse(actualizado);
    }

    @Override
    public void eliminar(UUID id) {
        log.info("Eliminando solicitante ID: {}", id);

        if (!solicitanteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Solicitante", "id", id);
        }

        solicitanteRepository.deleteById(id);
        log.info("Solicitante eliminado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitanteResponse> buscarPorNombre(String nombre) {
        log.debug("Buscando solicitantes por nombre: {}", nombre);

        List<Solicitante> solicitantes = solicitanteRepository.findByNombreCompletoContainingIgnoreCase(nombre);
        return solicitanteMapper.toResponseList(solicitantes);
    }
}
