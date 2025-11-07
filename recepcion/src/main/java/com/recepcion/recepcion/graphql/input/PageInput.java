package com.recepcion.recepcion.graphql.input;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Input para paginación en GraphQL
 */
@Data
public class PageInput {
    private Integer page = 0;
    private Integer size = 20;
    private String orderBy = "fechaReporte";
    private Sort.Direction direction = Sort.Direction.DESC;

    /**
     * Convierte el input a Pageable de Spring Data
     */
    public Pageable toPageable() {
        return PageRequest.of(
            page != null ? page : 0,
            size != null ? size : 20,
            Sort.by(direction != null ? direction : Sort.Direction.DESC, orderBy != null ? orderBy : "fechaReporte")
        );
    }
}
