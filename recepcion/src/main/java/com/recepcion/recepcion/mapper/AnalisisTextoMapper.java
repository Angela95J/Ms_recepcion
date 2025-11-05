package com.recepcion.recepcion.mapper;

import com.recepcion.recepcion.dto.response.AnalisisTextoResponse;
import com.recepcion.recepcion.entity.AnalisisMlTexto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnalisisTextoMapper {

    /**
     * Convierte entidad AnalisisMlTexto a AnalisisTextoResponse
     */
    AnalisisTextoResponse toResponse(AnalisisMlTexto entity);

    /**
     * Convierte lista de entidades a lista de responses
     */
    List<AnalisisTextoResponse> toResponseList(List<AnalisisMlTexto> entities);
}
