package com.recepcion.recepcion.service;

import com.recepcion.recepcion.dto.request.ActualizarEstadoIncidenteRequest;
import com.recepcion.recepcion.dto.request.ActualizarIncidenteRequest;
import com.recepcion.recepcion.dto.request.CrearIncidenteRequest;
import com.recepcion.recepcion.dto.response.IncidenteDetalleResponse;
import com.recepcion.recepcion.dto.response.IncidenteResponse;
import com.recepcion.recepcion.entity.EstadoIncidente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IncidenteService {

    // ==================== CRUD BÁSICO ====================

    /**
     * Crear un nuevo incidente (SOLICITANTE y ADMIN)
     * Estado inicial: RECIBIDO
     */
    IncidenteResponse crear(CrearIncidenteRequest request);

    /**
     * Buscar incidente por ID (vista básica)
     */
    IncidenteResponse buscarPorId(UUID id);

    /**
     * Buscar incidente por ID (vista detallada con relaciones)
     */
    IncidenteDetalleResponse buscarDetallePorId(UUID id);

    /**
     * Listar todos los incidentes con paginación
     */
    Page<IncidenteResponse> listarTodos(Pageable pageable);

    /**
     * Actualizar información del incidente (solo ADMIN)
     */
    IncidenteResponse actualizar(UUID id, ActualizarIncidenteRequest request);

    /**
     * Eliminar incidente (solo ADMIN)
     */
    void eliminar(UUID id);

    // ==================== GESTIÓN DE ESTADOS ====================

    /**
     * Cambiar estado del incidente con validaciones (ADMIN)
     */
    IncidenteResponse cambiarEstado(UUID id, ActualizarEstadoIncidenteRequest request);

    /**
     * Cancelar incidente (SOLICITANTE puede cancelar sus propios incidentes)
     */
    IncidenteResponse cancelar(UUID id, String motivo);

    /**
     * Aprobar incidente para despacho (ADMIN)
     */
    IncidenteResponse aprobar(UUID id);

    /**
     * Rechazar incidente (ADMIN)
     */
    IncidenteResponse rechazar(UUID id, String motivo);

    // ==================== CONSULTAS ESPECIALIZADAS ====================

    /**
     * Listar incidentes por estado
     */
    List<IncidenteResponse> listarPorEstado(EstadoIncidente estado);

    /**
     * Listar incidentes por estado con paginación
     */
    Page<IncidenteResponse> listarPorEstado(EstadoIncidente estado, Pageable pageable);

    /**
     * Listar incidentes por prioridad final
     */
    List<IncidenteResponse> listarPorPrioridad(Integer prioridad);

    /**
     * Listar incidentes aprobados y verosímiles para despacho
     * (ordenados por prioridad y fecha)
     */
    List<IncidenteResponse> listarParaDespacho();

    /**
     * Listar incidentes pendientes de análisis ML
     */
    List<IncidenteResponse> listarPendientesAnalisis();

    /**
     * Listar incidentes por solicitante
     */
    List<IncidenteResponse> listarPorSolicitante(UUID solicitanteId);

    /**
     * Listar incidentes por rango de fechas
     */
    List<IncidenteResponse> listarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Listar incidentes con prioridad alta (1 o 2)
     */
    List<IncidenteResponse> listarPrioridadAlta();

    // ==================== ANÁLISIS ML (Stubs para integración futura) ====================

    /**
     * Marcar incidente para análisis de texto
     */
    void enviarAAnalisisTexto(UUID id);

    /**
     * Marcar incidente para análisis de imagen
     */
    void enviarAAnalisisImagen(UUID id);

    /**
     * Procesar resultado de análisis de texto (llamado por servicio ML)
     */
    void procesarResultadoAnalisisTexto(UUID incidenteId, UUID analisisTextoId);

    /**
     * Calcular prioridad final combinando análisis de texto e imagen
     */
    void calcularPrioridadFinal(UUID id);

    // ==================== VALIDACIONES ====================

    /**
     * Verificar si un solicitante tiene incidentes activos
     */
    boolean solicitanteTieneIncidentesActivos(UUID solicitanteId);

    /**
     * Contar incidentes activos de un solicitante
     */
    long contarIncidentesActivosPorSolicitante(UUID solicitanteId);
}
