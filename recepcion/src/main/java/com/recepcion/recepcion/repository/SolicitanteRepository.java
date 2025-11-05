package com.recepcion.recepcion.repository;

import com.recepcion.recepcion.entity.CanalOrigen;
import com.recepcion.recepcion.entity.Solicitante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitanteRepository extends JpaRepository<Solicitante, UUID> {

    /**
     * Buscar solicitante por teléfono (único)
     */
    Optional<Solicitante> findByTelefono(String telefono);

    /**
     * Verificar si existe un solicitante con ese teléfono
     */
    boolean existsByTelefono(String telefono);

    /**
     * Buscar solicitantes por canal de origen
     */
    List<Solicitante> findByCanalOrigen(CanalOrigen canalOrigen);

    /**
     * Buscar solicitantes por nombre (búsqueda parcial)
     */
    List<Solicitante> findByNombreCompletoContainingIgnoreCase(String nombre);
}
