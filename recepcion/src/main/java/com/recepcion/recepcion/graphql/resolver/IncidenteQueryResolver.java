package com.recepcion.recepcion.graphql.resolver;

import com.recepcion.recepcion.dto.response.IncidenteDetalleResponse;
import com.recepcion.recepcion.dto.response.IncidenteResponse;
import com.recepcion.recepcion.entity.EstadoIncidente;
import com.recepcion.recepcion.graphql.input.IncidenteFilterInput;
import com.recepcion.recepcion.graphql.input.PageInput;
import com.recepcion.recepcion.service.IncidenteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Resolver de GraphQL para queries de Incidentes
 * Conecta las queries GraphQL con el servicio de incidentes existente
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class IncidenteQueryResolver {

    private final IncidenteService incidenteService;

    /**
     * Query: incidente(id: UUID!): Incidente
     * Obtiene un incidente por ID con todos sus detalles
     * USO: Microservicio Frontend (vista detalle), n8n (verificar estado)
     */
    @QueryMapping
    public IncidenteDetalleResponse incidente(@Argument UUID id) {
        log.info("GraphQL Query: incidente(id={})", id);
        return incidenteService.buscarDetallePorId(id);
    }

    /**
     * Query: incidentes(filtros: IncidenteFilter, paginacion: PageInput): IncidentePage!
     * Lista incidentes con filtros opcionales y paginación
     * USO: Microservicio Frontend (dashboard, listados)
     */
    @QueryMapping
    public Page<IncidenteResponse> incidentes(
            @Argument IncidenteFilterInput filtros,
            @Argument PageInput paginacion
    ) {
        log.info("GraphQL Query: incidentes(filtros={}, paginacion={})", filtros, paginacion);

        Pageable pageable = paginacion != null ? paginacion.toPageable()
                : PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "fechaReporte"));

        // Si no hay filtros, retornar todos
        if (filtros == null) {
            return incidenteService.listarTodos(pageable);
        }

        // Aplicar filtros según lo que venga
        if (filtros.getEstado() != null) {
            return incidenteService.listarPorEstado(filtros.getEstado(), pageable);
        }

        if (filtros.getSolicitanteId() != null) {
            List<IncidenteResponse> list = incidenteService.listarPorSolicitante(filtros.getSolicitanteId());
            return convertListToPage(list, pageable);
        }

        if (filtros.getFechaInicio() != null && filtros.getFechaFin() != null) {
            List<IncidenteResponse> list = incidenteService.listarPorRangoFechas(
                filtros.getFechaInicio(),
                filtros.getFechaFin()
            );
            return convertListToPage(list, pageable);
        }

        // Por defecto, retornar todos
        return incidenteService.listarTodos(pageable);
    }

    /**
     * Query: incidentesPorEstado(estado: EstadoIncidente!, paginacion: PageInput): IncidentePage!
     * Lista incidentes filtrados por estado
     * USO: Microservicio Frontend (filtros por estado)
     */
    @QueryMapping
    public Page<IncidenteResponse> incidentesPorEstado(
            @Argument EstadoIncidente estado,
            @Argument PageInput paginacion
    ) {
        log.info("GraphQL Query: incidentesPorEstado(estado={}, paginacion={})", estado, paginacion);

        Pageable pageable = paginacion != null ? paginacion.toPageable()
                : PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "fechaReporte"));

        return incidenteService.listarPorEstado(estado, pageable);
    }

    /**
     * Query: incidentesPorPrioridad(prioridad: Int!, paginacion: PageInput): IncidentePage!
     * Lista incidentes filtrados por prioridad
     * USO: Microservicio Frontend (filtros por prioridad)
     */
    @QueryMapping
    public Page<IncidenteResponse> incidentesPorPrioridad(
            @Argument Integer prioridad,
            @Argument PageInput paginacion
    ) {
        log.info("GraphQL Query: incidentesPorPrioridad(prioridad={}, paginacion={})", prioridad, paginacion);

        Pageable pageable = paginacion != null ? paginacion.toPageable()
                : PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "fechaReporte"));

        List<IncidenteResponse> list = incidenteService.listarPorPrioridad(prioridad);
        return convertListToPage(list, pageable);
    }

    /**
     * Query: incidentesPrioridadAlta(paginacion: PageInput): IncidentePage!
     * Lista incidentes de prioridad alta (prioridad >= 3)
     * USO: Microservicio Despacho (ambulancias urgentes), Frontend (alertas)
     */
    @QueryMapping
    public Page<IncidenteResponse> incidentesPrioridadAlta(@Argument PageInput paginacion) {
        log.info("GraphQL Query: incidentesPrioridadAlta(paginacion={})", paginacion);

        Pageable pageable = paginacion != null ? paginacion.toPageable()
                : PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "prioridadFinal"));

        List<IncidenteResponse> list = incidenteService.listarPrioridadAlta();
        return convertListToPage(list, pageable);
    }

    /**
     * Query: incidentesPendientesAnalisis(paginacion: PageInput): IncidentePage!
     * Lista incidentes pendientes de análisis ML
     * USO: Microservicio Frontend (panel de administración ML)
     */
    @QueryMapping
    public Page<IncidenteResponse> incidentesPendientesAnalisis(@Argument PageInput paginacion) {
        log.info("GraphQL Query: incidentesPendientesAnalisis(paginacion={})", paginacion);

        Pageable pageable = paginacion != null ? paginacion.toPageable()
                : PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "fechaReporte"));

        List<IncidenteResponse> list = incidenteService.listarPendientesAnalisis();
        return convertListToPage(list, pageable);
    }

    /**
     * Query: incidentesParaDespacho(paginacion: PageInput): IncidentePage!
     * Lista incidentes listos para despacho de ambulancia
     * USO: Microservicio Despacho (incidentes aprobados y analizados)
     */
    @QueryMapping
    public Page<IncidenteResponse> incidentesParaDespacho(@Argument PageInput paginacion) {
        log.info("GraphQL Query: incidentesParaDespacho(paginacion={})", paginacion);

        Pageable pageable = paginacion != null ? paginacion.toPageable()
                : PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "prioridadFinal"));

        List<IncidenteResponse> list = incidenteService.listarParaDespacho();
        return convertListToPage(list, pageable);
    }

    /**
     * Query: incidentesPorSolicitante(solicitanteId: UUID!, paginacion: PageInput): IncidentePage!
     * Lista todos los incidentes de un solicitante
     * USO: Microservicio Frontend (historial de solicitante)
     */
    @QueryMapping
    public Page<IncidenteResponse> incidentesPorSolicitante(
            @Argument UUID solicitanteId,
            @Argument PageInput paginacion
    ) {
        log.info("GraphQL Query: incidentesPorSolicitante(solicitanteId={}, paginacion={})",
                solicitanteId, paginacion);

        Pageable pageable = paginacion != null ? paginacion.toPageable()
                : PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "fechaReporte"));

        List<IncidenteResponse> list = incidenteService.listarPorSolicitante(solicitanteId);
        return convertListToPage(list, pageable);
    }

    /**
     * Query: incidentesPorRangoFechas(fechaInicio: DateTime!, fechaFin: DateTime!, paginacion: PageInput): IncidentePage!
     * Lista incidentes en un rango de fechas
     * USO: Microservicio Frontend (reportes, estadísticas)
     */
    @QueryMapping
    public Page<IncidenteResponse> incidentesPorRangoFechas(
            @Argument LocalDateTime fechaInicio,
            @Argument LocalDateTime fechaFin,
            @Argument PageInput paginacion
    ) {
        log.info("GraphQL Query: incidentesPorRangoFechas(fechaInicio={}, fechaFin={}, paginacion={})",
                fechaInicio, fechaFin, paginacion);

        Pageable pageable = paginacion != null ? paginacion.toPageable()
                : PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "fechaReporte"));

        List<IncidenteResponse> list = incidenteService.listarPorRangoFechas(fechaInicio, fechaFin);
        return convertListToPage(list, pageable);
    }

    /**
     * Convierte una List a Page para compatibilidad con GraphQL
     */
    private Page<IncidenteResponse> convertListToPage(List<IncidenteResponse> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        if (start > list.size()) {
            return new PageImpl<>(List.of(), pageable, list.size());
        }

        List<IncidenteResponse> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }
}
