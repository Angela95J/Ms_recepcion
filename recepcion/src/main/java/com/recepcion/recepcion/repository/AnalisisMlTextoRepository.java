package com.recepcion.recepcion.repository;

import com.recepcion.recepcion.entity.AnalisisMlTexto;
import com.recepcion.recepcion.entity.EstadoAnalisis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalisisMlTextoRepository extends JpaRepository<AnalisisMlTexto, UUID> {

    /**
     * Buscar análisis por incidente
     */
    @Query("SELECT a FROM AnalisisMlTexto a WHERE a.incidente.id = :incidenteId")
    Optional<AnalisisMlTexto> findByIncidenteId(UUID incidenteId);

    /**
     * Buscar análisis por estado
     */
    List<AnalisisMlTexto> findByEstadoAnalisis(EstadoAnalisis estado);

    /**
     * Buscar análisis por prioridad calculada
     */
    List<AnalisisMlTexto> findByPrioridadCalculada(Integer prioridad);

    /**
     * Buscar análisis con prioridad alta
     */
    @Query("SELECT a FROM AnalisisMlTexto a WHERE a.prioridadCalculada IN (1, 2)")
    List<AnalisisMlTexto> findAnalisisPrioridadAlta();

    /**
     * Buscar análisis con errores
     */
    List<AnalisisMlTexto> findByEstadoAnalisisAndErrorMensajeIsNotNull(EstadoAnalisis estado);
}
