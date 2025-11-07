package com.recepcion.recepcion.graphql.resolver;

import com.recepcion.recepcion.dto.response.SolicitanteResponse;
import com.recepcion.recepcion.graphql.input.PageInput;
import com.recepcion.recepcion.service.SolicitanteService;
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
 * Resolver de GraphQL para queries de Solicitantes
 * USO: Microservicio Frontend (gestión de solicitantes), verificar historial
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class SolicitanteQueryResolver {

    private final SolicitanteService solicitanteService;

    /**
     * Query: solicitante(id: UUID!): Solicitante
     * Obtiene un solicitante por ID
     * USO: Frontend (perfil de solicitante)
     */
    @QueryMapping
    public SolicitanteResponse solicitante(@Argument UUID id) {
        log.info("GraphQL Query: solicitante(id={})", id);
        return solicitanteService.buscarPorId(id);
    }

    /**
     * Query: solicitantes(paginacion: PageInput): SolicitantePage!
     * Lista todos los solicitantes
     * USO: Frontend (gestión de solicitantes)
     */
    @QueryMapping
    public Page<SolicitanteResponse> solicitantes(@Argument PageInput paginacion) {
        log.info("GraphQL Query: solicitantes(paginacion={})", paginacion);

        Pageable pageable = paginacion != null ? paginacion.toPageable()
                : PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "fechaRegistro"));

        return solicitanteService.listarTodos(pageable);
    }

    /**
     * Query: solicitantePorTelefono(telefono: String!): Solicitante
     * Busca un solicitante por número de teléfono
     * USO: n8n (verificar si solicitante existe), Frontend (búsqueda)
     */
    @QueryMapping
    public SolicitanteResponse solicitantePorTelefono(@Argument String telefono) {
        log.info("GraphQL Query: solicitantePorTelefono(telefono={})", telefono);
        try {
            return solicitanteService.buscarPorTelefono(telefono);
        } catch (Exception e) {
            log.warn("Solicitante no encontrado con telefono: {}", telefono);
            return null;
        }
    }
}
