package com.recepcion.recepcion.service.impl;

import com.recepcion.recepcion.dto.request.ActualizarEstadoIncidenteRequest;
import com.recepcion.recepcion.dto.request.ActualizarIncidenteRequest;
import com.recepcion.recepcion.dto.request.CrearIncidenteRequest;
import com.recepcion.recepcion.dto.response.IncidenteDetalleResponse;
import com.recepcion.recepcion.dto.response.IncidenteResponse;
import com.recepcion.recepcion.entity.*;
import com.recepcion.recepcion.event.IncidenteCreadoEvent;
import com.recepcion.recepcion.exception.BadRequestException;
//import com.recepcion.recepcion.exception.ConflictException;
import com.recepcion.recepcion.exception.ResourceNotFoundException;
import com.recepcion.recepcion.mapper.IncidenteMapper;
import com.recepcion.recepcion.mapper.SolicitanteMapper;
import com.recepcion.recepcion.mapper.UbicacionMapper;
import com.recepcion.recepcion.repository.*;
import com.recepcion.recepcion.service.AnalisisMlOrchestrationService;
import com.recepcion.recepcion.service.IncidenteService;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class IncidenteServiceImpl implements IncidenteService {

    private final IncidenteRepository incidenteRepository;
    private final SolicitanteRepository solicitanteRepository;
    private final UbicacionRepository ubicacionRepository;
    private final AnalisisMlTextoRepository analisisTextoRepository;
    private final MultimediaRepository multimediaRepository;
    private final AnalisisMlImagenRepository analisisImagenRepository;

    private final IncidenteMapper incidenteMapper;
    private final SolicitanteMapper solicitanteMapper;
    private final UbicacionMapper ubicacionMapper;

    private final AnalisisMlOrchestrationService analisisMlOrchestrationService;
    private final ApplicationEventPublisher eventPublisher;

    public IncidenteServiceImpl(
            IncidenteRepository incidenteRepository,
            SolicitanteRepository solicitanteRepository,
            UbicacionRepository ubicacionRepository,
            AnalisisMlTextoRepository analisisTextoRepository,
            MultimediaRepository multimediaRepository,
            AnalisisMlImagenRepository analisisImagenRepository,
            IncidenteMapper incidenteMapper,
            SolicitanteMapper solicitanteMapper,
            UbicacionMapper ubicacionMapper,
            @Lazy AnalisisMlOrchestrationService analisisMlOrchestrationService,
            ApplicationEventPublisher eventPublisher) {
        this.incidenteRepository = incidenteRepository;
        this.solicitanteRepository = solicitanteRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.analisisTextoRepository = analisisTextoRepository;
        this.multimediaRepository = multimediaRepository;
        this.analisisImagenRepository = analisisImagenRepository;
        this.incidenteMapper = incidenteMapper;
        this.solicitanteMapper = solicitanteMapper;
        this.ubicacionMapper = ubicacionMapper;
        this.analisisMlOrchestrationService = analisisMlOrchestrationService;
        this.eventPublisher = eventPublisher;
    }

    // ==================== CRUD BÁSICO ====================

    @Override
    public IncidenteResponse crear(CrearIncidenteRequest request) {
        log.info("Creando nuevo incidente");

        // 1. Crear o buscar solicitante por teléfono
        Solicitante solicitante = solicitanteRepository.findByTelefono(request.getSolicitante().getTelefono())
                .orElseGet(() -> {
                    log.info("Creando nuevo solicitante: {}", request.getSolicitante().getTelefono());
                    Solicitante nuevoSolicitante = solicitanteMapper.toEntity(request.getSolicitante());
                    return solicitanteRepository.save(nuevoSolicitante);
                });

        // 2. Crear ubicación
        Ubicacion ubicacion = ubicacionMapper.toEntity(request.getUbicacion());
        ubicacion = ubicacionRepository.save(ubicacion);

        // 3. Crear incidente
        Incidente incidente = Incidente.builder()
                .solicitante(solicitante)
                .ubicacion(ubicacion)
                .descripcionOriginal(request.getDescripcionOriginal())
                .tipoIncidenteReportado(request.getTipoIncidenteReportado())
                .estadoIncidente(EstadoIncidente.RECIBIDO)
                .prioridadInicial(3) // Prioridad media por defecto
                .build();

        incidente = incidenteRepository.save(incidente);

        log.info("Incidente creado exitosamente con ID: {}", incidente.getId());

        // 4. Publicar evento para análisis ML de texto (se ejecutará después del COMMIT)
        eventPublisher.publishEvent(new IncidenteCreadoEvent(this, incidente.getId()));
        log.info("Evento IncidenteCreadoEvent publicado para incidente: {}", incidente.getId());

        return incidenteMapper.toResponse(incidente);
    }

    @Override
    @Transactional(readOnly = true)
    public IncidenteResponse buscarPorId(UUID id) {
        log.debug("Buscando incidente por ID: {}", id);

        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        return incidenteMapper.toResponse(incidente);
    }

    @Override
    @Transactional(readOnly = true)
    public IncidenteDetalleResponse buscarDetallePorId(UUID id) {
        log.debug("Buscando detalle de incidente por ID: {}", id);

        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        return incidenteMapper.toDetalleResponse(incidente);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidenteResponse> listarTodos(Pageable pageable) {
        log.debug("Listando todos los incidentes - Página: {}", pageable.getPageNumber());

        return incidenteRepository.findAll(pageable)
                .map(incidenteMapper::toResponse);
    }

    @Override
    public IncidenteResponse actualizar(UUID id, ActualizarIncidenteRequest request) {
        log.info("Actualizando incidente ID: {}", id);

        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        // Actualizar campos permitidos
        if (request.getDescripcionOriginal() != null) {
            incidente.setDescripcionOriginal(request.getDescripcionOriginal());
        }
        if (request.getTipoIncidenteReportado() != null) {
            incidente.setTipoIncidenteReportado(request.getTipoIncidenteReportado());
        }
        if (request.getObservaciones() != null) {
            incidente.setObservaciones(request.getObservaciones());
        }

        incidente = incidenteRepository.save(incidente);

        log.info("Incidente actualizado exitosamente: {}", id);
        return incidenteMapper.toResponse(incidente);
    }

    @Override
    public void eliminar(UUID id) {
        log.info("Eliminando incidente ID: {} (solo ADMIN)", id);

        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        // Validar que no esté APROBADO
        if (incidente.getEstadoIncidente() == EstadoIncidente.APROBADO) {
            throw new BadRequestException("No se puede eliminar un incidente APROBADO. Debe cancelarlo primero.");
        }

        UUID solicitanteId = incidente.getSolicitante().getId();
        UUID ubicacionId = incidente.getUbicacion().getId();

        // 1. Eliminar análisis de imágenes y archivos multimedia
        log.debug("Eliminando multimedia e imágenes del incidente {}", id);
        List<Multimedia> multimediaList = multimediaRepository.findByIncidenteId(id);
        for (Multimedia multimedia : multimediaList) {
            // Eliminar análisis de imagen si existe
            analisisImagenRepository.findByMultimediaId(multimedia.getId()).ifPresent(analisisImagen -> {
                log.debug("Eliminando análisis de imagen ID: {}", analisisImagen.getId());
                analisisImagenRepository.delete(analisisImagen);
            });

            // Eliminar archivo físico
            try {
                Files.deleteIfExists(Paths.get(multimedia.getUrlArchivo()));
                log.debug("Archivo físico eliminado: {}", multimedia.getUrlArchivo());
            } catch (Exception e) {
                log.warn("No se pudo eliminar archivo físico: {}", multimedia.getUrlArchivo(), e);
            }

            // Eliminar miniatura si existe
            if (multimedia.getUrlMiniatura() != null) {
                try {
                    Files.deleteIfExists(Paths.get(multimedia.getUrlMiniatura()));
                    log.debug("Miniatura eliminada: {}", multimedia.getUrlMiniatura());
                } catch (Exception e) {
                    log.warn("No se pudo eliminar miniatura: {}", multimedia.getUrlMiniatura(), e);
                }
            }

            // Eliminar registro de multimedia
            multimediaRepository.delete(multimedia);
        }

        // 2. Eliminar análisis de texto si existe
        if (incidente.getAnalisisTexto() != null) {
            log.debug("Eliminando análisis de texto ID: {}", incidente.getAnalisisTexto().getId());
            analisisTextoRepository.delete(incidente.getAnalisisTexto());
        }

        // 3. Eliminar el incidente
        incidenteRepository.delete(incidente);
        log.info("Incidente eliminado exitosamente: {}", id);

        // 4. Eliminar ubicación si era su único incidente
        long incidentesConUbicacion = incidenteRepository.countByUbicacionId(ubicacionId);
        if (incidentesConUbicacion == 0) {
            log.info("Eliminando ubicación ID: {} - era el único incidente asociado", ubicacionId);
            ubicacionRepository.deleteById(ubicacionId);
        } else {
            log.debug("Ubicación ID: {} tiene {} incidentes adicionales - no se eliminará",
                     ubicacionId, incidentesConUbicacion);
        }

        // 5. Eliminar solicitante si era su único incidente
        long incidentesDelSolicitante = incidenteRepository.findBySolicitanteId(solicitanteId).size();
        if (incidentesDelSolicitante == 0) {
            log.info("Eliminando solicitante ID: {} - era el único incidente asociado", solicitanteId);
            solicitanteRepository.deleteById(solicitanteId);
        } else {
            log.debug("Solicitante ID: {} tiene {} incidentes adicionales - no se eliminará",
                     solicitanteId, incidentesDelSolicitante);
        }
    }

    // ==================== GESTIÓN DE ESTADOS ====================

    @Override
    public IncidenteResponse cambiarEstado(UUID id, ActualizarEstadoIncidenteRequest request) {
        log.info("Cambiando estado del incidente ID: {} a {}", id, request.getNuevoEstado());

        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        EstadoIncidente estadoActual = incidente.getEstadoIncidente();
        EstadoIncidente nuevoEstado = request.getNuevoEstado();

        // Validar transición de estado
        validarTransicionEstado(estadoActual, nuevoEstado);

        // Cambiar estado
        incidente.setEstadoIncidente(nuevoEstado);

        // Si es RECHAZADO, guardar motivo
        if (nuevoEstado == EstadoIncidente.RECHAZADO && request.getMotivoRechazo() != null) {
            incidente.setMotivoRechazo(request.getMotivoRechazo());
        }

        // Si es ANALIZADO, marcar fecha de análisis completado
        if (nuevoEstado == EstadoIncidente.ANALIZADO) {
            incidente.setFechaAnalisisCompletado(LocalDateTime.now());
        }

        // Agregar observaciones si las hay
        if (request.getObservaciones() != null) {
            incidente.setObservaciones(request.getObservaciones());
        }

        incidente = incidenteRepository.save(incidente);

        log.info("Estado cambiado exitosamente de {} a {}", estadoActual, nuevoEstado);
        return incidenteMapper.toResponse(incidente);
    }

    @Override
    public IncidenteResponse cancelar(UUID id, String motivo) {
        log.info("Cancelando incidente ID: {} (SOLICITANTE puede cancelar)", id);

        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        // Validar que no esté en estados finales
        if (incidente.getEstadoIncidente() == EstadoIncidente.RECHAZADO ||
            incidente.getEstadoIncidente() == EstadoIncidente.CANCELADO) {
            throw new BadRequestException("El incidente ya está en estado final: " + incidente.getEstadoIncidente());
        }

        // Validar que no esté APROBADO (requiere autorización ADMIN)
        if (incidente.getEstadoIncidente() == EstadoIncidente.APROBADO) {
            throw new BadRequestException("No se puede cancelar un incidente APROBADO. Contacte al administrador.");
        }

        incidente.setEstadoIncidente(EstadoIncidente.CANCELADO);
        incidente.setMotivoRechazo(motivo != null ? motivo : "Cancelado por solicitante");

        incidente = incidenteRepository.save(incidente);

        log.info("Incidente cancelado exitosamente: {}", id);
        return incidenteMapper.toResponse(incidente);
    }

    @Override
    public IncidenteResponse aprobar(UUID id) {
        log.info("Aprobando incidente ID: {} para despacho", id);

        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        // Validar que esté ANALIZADO
        if (incidente.getEstadoIncidente() != EstadoIncidente.ANALIZADO) {
            throw new BadRequestException("Solo se pueden aprobar incidentes en estado ANALIZADO");
        }

        // Validar que sea verosímil (si ya se analizó)
        if (incidente.getScoreVeracidad() != null && !incidente.getEsVerosimil()) {
            throw new BadRequestException("No se puede aprobar un incidente con baja veracidad");
        }

        incidente.setEstadoIncidente(EstadoIncidente.APROBADO);
        incidente = incidenteRepository.save(incidente);

        log.info("Incidente aprobado exitosamente: {}", id);
        return incidenteMapper.toResponse(incidente);
    }

    @Override
    public IncidenteResponse rechazar(UUID id, String motivo) {
        log.info("Rechazando incidente ID: {}", id);

        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        // Validar que no esté APROBADO
        if (incidente.getEstadoIncidente() == EstadoIncidente.APROBADO) {
            throw new BadRequestException("No se puede rechazar un incidente ya APROBADO");
        }

        incidente.setEstadoIncidente(EstadoIncidente.RECHAZADO);
        incidente.setMotivoRechazo(motivo);
        incidente = incidenteRepository.save(incidente);

        log.info("Incidente rechazado exitosamente: {}", id);
        return incidenteMapper.toResponse(incidente);
    }

    // ==================== CONSULTAS ESPECIALIZADAS ====================

    @Override
    @Transactional(readOnly = true)
    public List<IncidenteResponse> listarPorEstado(EstadoIncidente estado) {
        log.debug("Listando incidentes por estado: {}", estado);

        List<Incidente> incidentes = incidenteRepository.findByEstadoIncidente(estado);
        return incidenteMapper.toResponseList(incidentes);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidenteResponse> listarPorEstado(EstadoIncidente estado, Pageable pageable) {
        log.debug("Listando incidentes por estado: {} - Página: {}", estado, pageable.getPageNumber());

        return incidenteRepository.findByEstadoIncidente(estado, pageable)
                .map(incidenteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenteResponse> listarPorPrioridad(Integer prioridad) {
        log.debug("Listando incidentes por prioridad: {}", prioridad);

        if (prioridad < 1 || prioridad > 5) {
            throw new BadRequestException("La prioridad debe estar entre 1 y 5");
        }

        List<Incidente> incidentes = incidenteRepository.findByPrioridadFinalOrderByFechaReporteAsc(prioridad);
        return incidenteMapper.toResponseList(incidentes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenteResponse> listarParaDespacho() {
        log.debug("Listando incidentes aprobados para despacho");

        List<Incidente> incidentes = incidenteRepository.findIncidentesParaDespacho();
        return incidenteMapper.toResponseList(incidentes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenteResponse> listarPendientesAnalisis() {
        log.debug("Listando incidentes pendientes de análisis ML");

        List<Incidente> incidentes = incidenteRepository.findIncidentesPendientesAnalisisTexto();
        return incidenteMapper.toResponseList(incidentes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenteResponse> listarPorSolicitante(UUID solicitanteId) {
        log.debug("Listando incidentes del solicitante: {}", solicitanteId);

        List<Incidente> incidentes = incidenteRepository.findBySolicitanteId(solicitanteId);
        return incidenteMapper.toResponseList(incidentes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenteResponse> listarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.debug("Listando incidentes entre {} y {}", fechaInicio, fechaFin);

        if (fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("La fecha de inicio debe ser anterior a la fecha fin");
        }

        List<Incidente> incidentes = incidenteRepository.findByFechaReporteBetween(fechaInicio, fechaFin);
        return incidenteMapper.toResponseList(incidentes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenteResponse> listarPrioridadAlta() {
        log.debug("Listando incidentes con prioridad alta (1 o 2)");

        List<Incidente> incidentes = incidenteRepository.findIncidentesPrioridadAlta();
        return incidenteMapper.toResponseList(incidentes);
    }

    // ==================== ANÁLISIS ML (Stubs) ====================

    @Override
    public void enviarAAnalisisTexto(UUID id) {
        log.info("Marcando incidente ID: {} para análisis de texto", id);

        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        incidente.setEstadoIncidente(EstadoIncidente.EN_ANALISIS_TEXTO);
        incidenteRepository.save(incidente);

        // TODO: Aquí se enviará a cola/servicio ML externo
        log.info("Incidente enviado a análisis de texto (stub para integración futura)");
    }

    @Override
    public void enviarAAnalisisImagen(UUID id) {
        log.info("Marcando incidente ID: {} para análisis de imagen", id);

        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        incidente.setEstadoIncidente(EstadoIncidente.EN_ANALISIS_IMAGEN);
        incidenteRepository.save(incidente);

        // TODO: Aquí se enviará a cola/servicio ML externo
        log.info("Incidente enviado a análisis de imagen (stub para integración futura)");
    }

    @Override
    public void procesarResultadoAnalisisTexto(UUID incidenteId, UUID analisisTextoId) {
        log.info("Procesando resultado de análisis de texto para incidente: {}", incidenteId);

        Incidente incidente = incidenteRepository.findById(incidenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", incidenteId));

        AnalisisMlTexto analisis = analisisTextoRepository.findById(analisisTextoId)
                .orElseThrow(() -> new ResourceNotFoundException("AnalisisMlTexto", "id", analisisTextoId));

        // Asignar análisis al incidente
        incidente.setAnalisisTexto(analisis);
        incidente.setPrioridadTexto(analisis.getPrioridadCalculada());
        incidente.setTipoIncidenteClasificado(analisis.getTipoIncidentePredicho());

        // Calcular prioridad final
        calcularPrioridadFinal(incidenteId);

        incidenteRepository.save(incidente);
        log.info("Resultado de análisis de texto procesado exitosamente");
    }

    @Override
    public void calcularPrioridadFinal(UUID id) {
        log.debug("Calculando prioridad final para incidente: {}", id);

        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        Integer prioridadTexto = incidente.getPrioridadTexto();
        Integer prioridadImagen = incidente.getPrioridadImagen();

        if (prioridadTexto != null && prioridadImagen != null) {
            // Combinar ambas prioridades (promedio ponderado)
            int prioridadFinal = (int) Math.round((prioridadTexto * 0.6 + prioridadImagen * 0.4));
            incidente.setPrioridadFinal(prioridadFinal);
            log.info("Prioridad final calculada: {}", prioridadFinal);
        } else if (prioridadTexto != null) {
            // Solo hay análisis de texto
            incidente.setPrioridadFinal(prioridadTexto);
        } else if (prioridadImagen != null) {
            // Solo hay análisis de imagen
            incidente.setPrioridadFinal(prioridadImagen);
        }

        incidenteRepository.save(incidente);
    }

    // ==================== VALIDACIONES ====================

    @Override
    @Transactional(readOnly = true)
    public boolean solicitanteTieneIncidentesActivos(UUID solicitanteId) {
        return incidenteRepository.solicitanteTieneIncidentesActivos(solicitanteId);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarIncidentesActivosPorSolicitante(UUID solicitanteId) {
        return incidenteRepository.countIncidentesActivosPorSolicitante(solicitanteId);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Valida que la transición de estado sea permitida
     */
    private void validarTransicionEstado(EstadoIncidente actual, EstadoIncidente nuevo) {
        // Estados finales no pueden cambiar
        if (actual == EstadoIncidente.RECHAZADO || actual == EstadoIncidente.CANCELADO) {
            throw new BadRequestException("No se puede cambiar el estado de un incidente en estado final: " + actual);
        }

        // No se puede regresar a RECIBIDO
        if (nuevo == EstadoIncidente.RECIBIDO && actual != EstadoIncidente.RECIBIDO) {
            throw new BadRequestException("No se puede regresar al estado RECIBIDO");
        }

        // Validar flujo: RECIBIDO → EN_ANALISIS_TEXTO → EN_ANALISIS_IMAGEN → ANALIZADO → APROBADO/RECHAZADO
        // (Permitimos saltar pasos si es necesario, pero no regresar)
        log.debug("Transición de estado validada: {} → {}", actual, nuevo);
    }
}
