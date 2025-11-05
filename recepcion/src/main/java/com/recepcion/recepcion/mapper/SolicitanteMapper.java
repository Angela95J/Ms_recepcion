package com.recepcion.recepcion.mapper;

import com.recepcion.recepcion.dto.request.SolicitanteRequest;
import com.recepcion.recepcion.dto.response.SolicitanteResponse;
import com.recepcion.recepcion.entity.Solicitante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SolicitanteMapper {

    /**
     * Convierte SolicitanteRequest a entidad Solicitante
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "incidentes", ignore = true)
    Solicitante toEntity(SolicitanteRequest request);

    /**
     * Convierte entidad Solicitante a SolicitanteResponse
     */
    SolicitanteResponse toResponse(Solicitante entity);

    /**
     * Convierte lista de entidades a lista de responses
     */
    List<SolicitanteResponse> toResponseList(List<Solicitante> entities);

    /**
     * Actualiza una entidad existente con datos del request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "incidentes", ignore = true)
    void updateEntityFromRequest(SolicitanteRequest request, @MappingTarget Solicitante entity);
}
