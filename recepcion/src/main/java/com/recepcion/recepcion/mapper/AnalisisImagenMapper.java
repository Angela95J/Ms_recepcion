package com.recepcion.recepcion.mapper;

import com.recepcion.recepcion.dto.response.AnalisisImagenResponse;
import com.recepcion.recepcion.entity.AnalisisMlImagen;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnalisisImagenMapper {

    /**
     * Convierte entidad AnalisisMlImagen a AnalisisImagenResponse
     */
    @Mapping(source = "multimedia.id", target = "multimediaId")
    AnalisisImagenResponse toResponse(AnalisisMlImagen entity);

    /**
     * Convierte lista de entidades a lista de responses
     */
    List<AnalisisImagenResponse> toResponseList(List<AnalisisMlImagen> entities);
}
