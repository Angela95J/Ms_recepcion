package com.recepcion.recepcion.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro para validar API Keys en las peticiones
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${api.keys.admin}")
    private String adminApiKey;

    @Value("${api.keys.n8n}")
    private String n8nApiKey;

    private static final String API_KEY_HEADER = "X-API-Key";

    // Rutas que NO requieren API Key (públicas)
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/actuator",
            "/api/v3/api-docs",
            "/api/swagger-ui",
            "/api/swagger-resources"
    );

    // Rutas que pueden usar API Key de n8n (creación de incidentes y gestión de conversaciones)
    private static final List<String> N8N_PATHS = Arrays.asList(
            "/api/incidentes",
            "/api/solicitantes",
            "/api/ubicaciones",
            "/api/multimedia",
            "/api/conversaciones"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        // Permitir rutas públicas sin API Key
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Obtener API Key del header
        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("Petición sin API Key a: {} {}", method, requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"API Key requerida. Use header X-API-Key\"}");
            return;
        }

        // Validar API Key
        boolean isValidAdmin = apiKey.equals(adminApiKey);
        boolean isValidN8n = apiKey.equals(n8nApiKey);

        if (!isValidAdmin && !isValidN8n) {
            log.warn("API Key inválida en petición a: {} {}", method, requestPath);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"API Key inválida\"}");
            return;
        }

        // Validar permisos según el tipo de API Key
        if (isValidN8n && !isN8nAllowedPath(requestPath, method)) {
            log.warn("API Key de n8n intentando acceder a ruta no permitida: {} {}", method, requestPath);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Acceso no permitido con esta API Key\"}");
            return;
        }

        // Agregar atributo para identificar el tipo de API Key
        request.setAttribute("API_KEY_TYPE", isValidAdmin ? "ADMIN" : "N8N");

        log.debug("Petición autenticada con API Key tipo: {}", isValidAdmin ? "ADMIN" : "N8N");

        filterChain.doFilter(request, response);
    }

    /**
     * Verificar si la ruta es pública
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Verificar si n8n puede acceder a esta ruta
     * n8n solo puede crear recursos (POST) en rutas específicas y ver/descargar multimedia
     */
    private boolean isN8nAllowedPath(String path, String method) {
        // n8n puede hacer POST en rutas de creación
        if ("POST".equals(method)) {
            return N8N_PATHS.stream().anyMatch(path::startsWith);
        }

        // n8n puede hacer GET en multimedia (para verificar subida)
        if ("GET".equals(method) && path.startsWith("/api/multimedia")) {
            return true;
        }

        // n8n puede hacer GET en incidentes (para consultar estado de análisis ML)
        if ("GET".equals(method) && path.startsWith("/api/incidentes")) {
            return true;
        }

        // n8n puede hacer GET, PATCH, DELETE en conversaciones (gestión de estado del bot)
        if (path.startsWith("/api/conversaciones")) {
            return "GET".equals(method) || "PATCH".equals(method) || "DELETE".equals(method) || "POST".equals(method);
        }

        // n8n NO puede hacer PUT, PATCH, DELETE en otras rutas
        return false;
    }
}
