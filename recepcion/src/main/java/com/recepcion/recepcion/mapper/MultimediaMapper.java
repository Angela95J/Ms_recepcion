package com.recepcion.recepcion.mapper;

import com.recepcion.recepcion.dto.response.MultimediaResponse;
import com.recepcion.recepcion.entity.Multimedia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MultimediaMapper {

    /**
     * Convierte entidad Multimedia a MultimediaResponse
     */
    @Mapping(source = "incidente.id", target = "incidenteId")
    MultimediaResponse toResponse(Multimedia entity);

    /**
     * Convierte lista de entidades a lista de responses
     */
    List<MultimediaResponse> toResponseList(List<Multimedia> entities);
}
