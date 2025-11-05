package com.recepcion.recepcion.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación automática de la API
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.multimedia.base-url:http://localhost:8080/api}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url(baseUrl)
                                .description("Servidor de Desarrollo")
                ))
                .components(new Components()
                        .addSecuritySchemes("X-API-KEY", apiKeyScheme())
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("X-API-KEY")
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Microservicio de Recepción de Incidentes")
                .version("1.0.0")
                .description("""
                        API REST para la recepción, validación y gestión de incidentes médicos
                        reportados a través de bots de WhatsApp y Telegram.

                        ## Autenticación

                        Esta API utiliza **API Keys** para autenticación. Hay dos tipos:

                        - **API Key ADMIN**: Acceso completo a todos los endpoints
                        - **API Key N8N**: Solo puede crear recursos (POST)

                        Incluir el header: `X-API-KEY: <tu-api-key>`

                        ## API Keys por Defecto (Desarrollo)

                        - **ADMIN**: `admin-key-change-in-production-12345`
                        - **N8N**: `n8n-key-change-in-production-67890`

                        ⚠️ **IMPORTANTE**: Cambiar estas claves en producción

                        ## Flujo de Trabajo

                        1. Bot recibe mensaje de solicitante (WhatsApp/Telegram)
                        2. n8n crea incidente via API (POST /incidentes)
                        3. n8n sube multimedia (POST /multimedia/incidente/{id}/subir)
                        4. Servicio ML analiza texto e imagen
                        5. Sistema calcula prioridad final
                        6. Administrador aprueba/rechaza incidente
                        7. Incidente listo para despacho de ambulancia
                        """)
                .contact(new Contact()
                        .name("Equipo de Desarrollo")
                        .email("soporte@ambulancia.com")
                )
                .license(new License()
                        .name("Proyecto Académico")
                        .url("https://github.com/tu-repo")
                );
    }

    private SecurityScheme apiKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-KEY")
                .description("""
                        API Key para autenticación. Usar una de las siguientes:

                        - **ADMIN** (acceso completo): `admin-key-change-in-production-12345`
                        - **N8N** (solo crear recursos): `n8n-key-change-in-production-67890`
                        """);
    }
}
