package com.recepcion.recepcion.mapper;

import com.recepcion.recepcion.dto.response.IncidenteDetalleResponse;
import com.recepcion.recepcion.dto.response.IncidenteResponse;
import com.recepcion.recepcion.entity.Incidente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
    SolicitanteMapper.class,
    UbicacionMapper.class,
    MultimediaMapper.class,
    AnalisisTextoMapper.class,
    HistorialEstadosMapper.class
})
public interface IncidenteMapper {

    /**
     * Convierte entidad Incidente a IncidenteResponse (vista básica)
     */
    @Named("toResponse")
    IncidenteResponse toResponse(Incidente entity);

    /**
     * Convierte entidad Incidente a IncidenteDetalleResponse (vista completa)
     */
    @Mapping(source = "analisisTexto", target = "analisisTexto")
    @Mapping(source = "multimedia", target = "multimedia")
    @Mapping(source = "historialEstados", target = "historialEstados")
    IncidenteDetalleResponse toDetalleResponse(Incidente entity);

    /**
     * Convierte lista de entidades a lista de responses básicos
     */
    @Named("toResponseList")
    List<IncidenteResponse> toResponseList(List<Incidente> entities);
}
