package com.recepcion.recepcion.repository;

import com.recepcion.recepcion.entity.AnalisisMlImagen;
import com.recepcion.recepcion.entity.EstadoAnalisis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalisisMlImagenRepository extends JpaRepository<AnalisisMlImagen, UUID> {

    /**
     * Buscar análisis por multimedia
     */
    Optional<AnalisisMlImagen> findByMultimediaId(UUID multimediaId);

    /**
     * Buscar análisis de imágenes de un incidente
     */
    @Query("SELECT ai FROM AnalisisMlImagen ai JOIN ai.multimedia m WHERE m.incidente.id = :incidenteId")
    List<AnalisisMlImagen> findByIncidenteId(@Param("incidenteId") UUID incidenteId);

    /**
     * Buscar análisis por estado
     */
    List<AnalisisMlImagen> findByEstadoAnalisis(EstadoAnalisis estado);

    /**
     * Buscar análisis con score de veracidad bajo (posibles falsas alarmas)
     */
    @Query("SELECT ai FROM AnalisisMlImagen ai WHERE ai.scoreVeracidad < :umbral")
    List<AnalisisMlImagen> findConVeracidadBaja(@Param("umbral") BigDecimal umbral);

    /**
     * Buscar análisis marcados como anomalías
     */
    List<AnalisisMlImagen> findByEsAnomaliaTrue();

    /**
     * Buscar análisis donde NO es imagen de accidente
     */
    List<AnalisisMlImagen> findByEsImagenAccidenteFalse();

    /**
     * Buscar análisis con gravedad visual alta
     */
    @Query("SELECT ai FROM AnalisisMlImagen ai WHERE ai.nivelGravedadVisual IN (1, 2)")
    List<AnalisisMlImagen> findConGravedadVisualAlta();

    /**
     * Contar análisis por incidente
     */
    @Query("SELECT COUNT(ai) FROM AnalisisMlImagen ai JOIN ai.multimedia m WHERE m.incidente.id = :incidenteId")
    long countByIncidenteId(@Param("incidenteId") UUID incidenteId);
}
