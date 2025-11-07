package com.recepcion.recepcion.graphql.resolver;

import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolver básico para probar que GraphQL funciona
 */
@Controller
public class HealthQueryResolver {

    @QueryMapping
    public String health() {
        return "GraphQL API is running! ✅";
    }
}
