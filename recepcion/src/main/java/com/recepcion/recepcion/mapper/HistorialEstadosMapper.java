package com.recepcion.recepcion.mapper;

import com.recepcion.recepcion.dto.response.HistorialEstadoResponse;
import com.recepcion.recepcion.entity.IncidenteHistorialEstados;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HistorialEstadosMapper {

    /**
     * Convierte entidad IncidenteHistorialEstados a HistorialEstadoResponse
     */
    HistorialEstadoResponse toResponse(IncidenteHistorialEstados entity);

    /**
     * Convierte lista de entidades a lista de responses
     */
    List<HistorialEstadoResponse> toResponseList(List<IncidenteHistorialEstados> entities);
}
