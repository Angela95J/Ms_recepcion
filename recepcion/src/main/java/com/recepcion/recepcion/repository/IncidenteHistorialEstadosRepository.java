package com.recepcion.recepcion.repository;

import com.recepcion.recepcion.entity.IncidenteHistorialEstados;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IncidenteHistorialEstadosRepository extends JpaRepository<IncidenteHistorialEstados, UUID> {

    /**
     * Buscar historial de un incidente ordenado por fecha
     */
    List<IncidenteHistorialEstados> findByIncidenteIdOrderByFechaCambioDesc(UUID incidenteId);

    /**
     * Buscar cambios realizados por un usuario
     */
    List<IncidenteHistorialEstados> findByUsuarioCambio(String usuarioCambio);

    /**
     * Buscar cambios por rango de fechas
     */
    @Query("SELECT h FROM IncidenteHistorialEstados h WHERE h.fechaCambio BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY h.fechaCambio DESC")
    List<IncidenteHistorialEstados> findByFechaCambioBetween(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Buscar cambios a un estado específico
     */
    List<IncidenteHistorialEstados> findByEstadoNuevo(String estadoNuevo);

    /**
     * Contar cambios de estado de un incidente
     */
    long countByIncidenteId(UUID incidenteId);

    /**
     * Obtener último cambio de estado de un incidente
     */
    @Query("SELECT h FROM IncidenteHistorialEstados h WHERE h.incidente.id = :incidenteId " +
           "ORDER BY h.fechaCambio DESC LIMIT 1")
    IncidenteHistorialEstados findUltimoCambio(@Param("incidenteId") UUID incidenteId);
}
