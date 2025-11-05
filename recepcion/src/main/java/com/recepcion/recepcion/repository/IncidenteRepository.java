package com.recepcion.recepcion.repository;

import com.recepcion.recepcion.entity.EstadoIncidente;
import com.recepcion.recepcion.entity.Incidente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncidenteRepository extends JpaRepository<Incidente, UUID> {

    /**
     * Buscar incidentes por estado
     */
    List<Incidente> findByEstadoIncidente(EstadoIncidente estado);

    /**
     * Buscar incidentes por estado con paginación
     */
    Page<Incidente> findByEstadoIncidente(EstadoIncidente estado, Pageable pageable);

    /**
     * Buscar incidentes por prioridad final ordenados por fecha
     */
    List<Incidente> findByPrioridadFinalOrderByFechaReporteAsc(Integer prioridad);

    /**
     * Buscar incidentes por solicitante
     */
    List<Incidente> findBySolicitanteId(UUID solicitanteId);

    /**
     * Buscar incidentes aprobados y verosímiles para despacho
     */
    @Query("SELECT i FROM Incidente i WHERE i.estadoIncidente = 'APROBADO' " +
           "AND i.esVerosimil = true ORDER BY i.prioridadFinal ASC, i.fechaReporte ASC")
    List<Incidente> findIncidentesParaDespacho();

    /**
     * Buscar incidentes pendientes de análisis de texto
     */
    @Query("SELECT i FROM Incidente i WHERE i.analisisTexto IS NULL " +
           "AND i.estadoIncidente IN ('RECIBIDO', 'EN_ANALISIS_TEXTO')")
    List<Incidente> findIncidentesPendientesAnalisisTexto();

    /**
     * Buscar incidentes por rango de fechas
     */
    @Query("SELECT i FROM Incidente i WHERE i.fechaReporte BETWEEN :fechaInicio AND :fechaFin")
    List<Incidente> findByFechaReporteBetween(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Contar incidentes por estado
     */
    long countByEstadoIncidente(EstadoIncidente estado);

    /**
     * Buscar incidentes con prioridad alta (1 o 2)
     */
    @Query("SELECT i FROM Incidente i WHERE i.prioridadFinal IN (1, 2) " +
           "ORDER BY i.prioridadFinal ASC, i.fechaReporte ASC")
    List<Incidente> findIncidentesPrioridadAlta();

    /**
     * Buscar incidentes por solicitante con paginación
     */
    Page<Incidente> findBySolicitanteId(UUID solicitanteId, Pageable pageable);

    /**
     * Verificar si un solicitante tiene incidentes activos (no finalizados)
     */
    @Query("SELECT COUNT(i) > 0 FROM Incidente i WHERE i.solicitante.id = :solicitanteId " +
           "AND i.estadoIncidente NOT IN ('RECHAZADO', 'CANCELADO')")
    boolean solicitanteTieneIncidentesActivos(@Param("solicitanteId") UUID solicitanteId);

    /**
     * Contar incidentes activos de un solicitante
     */
    @Query("SELECT COUNT(i) FROM Incidente i WHERE i.solicitante.id = :solicitanteId " +
           "AND i.estadoIncidente NOT IN ('RECHAZADO', 'CANCELADO')")
    long countIncidentesActivosPorSolicitante(@Param("solicitanteId") UUID solicitanteId);

    /**
     * Contar incidentes por ubicación
     */
    @Query("SELECT COUNT(i) FROM Incidente i WHERE i.ubicacion.id = :ubicacionId")
    long countByUbicacionId(@Param("ubicacionId") UUID ubicacionId);
}
