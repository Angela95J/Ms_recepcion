package com.recepcion.recepcion.graphql.resolver;

import com.recepcion.recepcion.dto.response.UbicacionResponse;
import com.recepcion.recepcion.graphql.input.PageInput;
import com.recepcion.recepcion.service.UbicacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * Resolver de GraphQL para queries de Ubicaciones
 * USO: Microservicio Despacho (coordenadas), Frontend (mapas)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class UbicacionQueryResolver {

    private final UbicacionService ubicacionService;

    /**
     * Query: ubicacion(id: UUID!): Ubicacion
     * Obtiene una ubicación por ID
     * USO: Despacho (obtener coordenadas exactas)
     */
    @QueryMapping
    public UbicacionResponse ubicacion(@Argument UUID id) {
        log.info("GraphQL Query: ubicacion(id={})", id);
        return ubicacionService.buscarPorId(id);
    }

    /**
     * Query: ubicaciones(paginacion: PageInput): UbicacionPage!
     * Lista todas las ubicaciones
     * USO: Frontend (gestión de ubicaciones)
     */
    @QueryMapping
    public Page<UbicacionResponse> ubicaciones(@Argument PageInput paginacion) {
        log.info("GraphQL Query: ubicaciones(paginacion={})", paginacion);

        Pageable pageable = paginacion != null ? paginacion.toPageable()
                : PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));

        return ubicacionService.listarTodos(pageable);
    }
}
