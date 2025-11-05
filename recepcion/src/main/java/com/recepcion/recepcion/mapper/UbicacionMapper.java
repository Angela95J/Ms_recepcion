package com.recepcion.recepcion.mapper;

import com.recepcion.recepcion.dto.request.UbicacionRequest;
import com.recepcion.recepcion.dto.response.UbicacionResponse;
import com.recepcion.recepcion.entity.Ubicacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UbicacionMapper {

    /**
     * Convierte UbicacionRequest a entidad Ubicacion
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "incidentes", ignore = true)
    Ubicacion toEntity(UbicacionRequest request);

    /**
     * Convierte entidad Ubicacion a UbicacionResponse
     */
    UbicacionResponse toResponse(Ubicacion entity);

    /**
     * Convierte lista de entidades a lista de responses
     */
    List<UbicacionResponse> toResponseList(List<Ubicacion> entities);

    /**
     * Actualiza una entidad existente con datos del request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "incidentes", ignore = true)
    void updateEntityFromRequest(UbicacionRequest request, @MappingTarget Ubicacion entity);
}
