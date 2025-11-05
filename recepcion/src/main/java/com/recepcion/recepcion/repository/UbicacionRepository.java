package com.recepcion.recepcion.repository;

import com.recepcion.recepcion.entity.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, UUID> {

    /**
     * Buscar ubicaciones por distrito
     */
    List<Ubicacion> findByDistrito(String distrito);

    /**
     * Buscar ubicaciones por zona
     */
    List<Ubicacion> findByZona(String zona);

    /**
     * Buscar ubicaciones por distrito y zona
     */
    List<Ubicacion> findByDistritoAndZona(String distrito, String zona);

    /**
     * Buscar ubicaciones que tengan coordenadas
     */
    @Query("SELECT u FROM Ubicacion u WHERE u.latitud IS NOT NULL AND u.longitud IS NOT NULL")
    List<Ubicacion> findUbicacionesConCoordenadas();
}
