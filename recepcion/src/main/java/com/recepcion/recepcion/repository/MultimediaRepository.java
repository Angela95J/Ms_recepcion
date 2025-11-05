package com.recepcion.recepcion.repository;

import com.recepcion.recepcion.entity.Multimedia;
import com.recepcion.recepcion.entity.TipoArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MultimediaRepository extends JpaRepository<Multimedia, UUID> {

    /**
     * Buscar todos los archivos multimedia de un incidente
     */
    List<Multimedia> findByIncidenteId(UUID incidenteId);

    /**
     * Buscar archivos multimedia por tipo
     */
    List<Multimedia> findByTipoArchivo(TipoArchivo tipoArchivo);

    /**
     * Buscar archivos multimedia pendientes de análisis ML
     */
    @Query("SELECT m FROM Multimedia m WHERE m.requiereAnalisisMl = true " +
           "AND m.analisisCompletado = false")
    List<Multimedia> findPendientesAnalisis();

    /**
     * Buscar la imagen principal de un incidente
     */
    Optional<Multimedia> findByIncidenteIdAndEsPrincipalTrue(UUID incidenteId);

    /**
     * Contar archivos multimedia por incidente
     */
    long countByIncidenteId(UUID incidenteId);

    /**
     * Buscar imágenes de un incidente
     */
    List<Multimedia> findByIncidenteIdAndTipoArchivo(UUID incidenteId, TipoArchivo tipoArchivo);

    /**
     * Verificar si un incidente tiene imágenes
     */
    @Query("SELECT COUNT(m) > 0 FROM Multimedia m WHERE m.incidente.id = :incidenteId " +
           "AND m.tipoArchivo = 'IMAGEN'")
    boolean incidenteTieneImagenes(UUID incidenteId);
}
