package com.recepcion.recepcion.client;

import com.recepcion.recepcion.dto.ml.AnalizarTextoRequest;
import com.recepcion.recepcion.dto.ml.AnalizarTextoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@Slf4j
public class MlTextoClient {

    private final RestTemplate restTemplate;
    private final String mlTextoBaseUrl;

    public MlTextoClient(
            RestTemplate restTemplate,
            @Value("${ml.texto.base-url:http://localhost:8001}") String mlTextoBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.mlTextoBaseUrl = mlTextoBaseUrl;
    }

    /**
     * Analiza el texto de un incidente llamando al servicio ML de Python
     *
     * @param texto Texto a analizar
     * @param incidenteId ID del incidente (opcional)
     * @return Response con análisis completo
     * @throws MlServiceException si el servicio ML falla
     */
    public AnalizarTextoResponse analizarTexto(String texto, UUID incidenteId) throws MlServiceException {
        String url = mlTextoBaseUrl + "/api/ml/analizar-texto";

        try {
            log.info("Llamando al servicio ML de texto: {}", url);

            // Construir request
            AnalizarTextoRequest request = AnalizarTextoRequest.builder()
                    .texto(texto)
                    .incidenteId(incidenteId)
                    .build();

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<AnalizarTextoRequest> entity = new HttpEntity<>(request, headers);

            // Llamada HTTP POST
            ResponseEntity<AnalizarTextoResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    AnalizarTextoResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Análisis de texto completado exitosamente. Prioridad: {}",
                        response.getBody().getPrioridadCalculada());
                return response.getBody();
            } else {
                throw new MlServiceException("Respuesta inválida del servicio ML de texto");
            }

        } catch (HttpClientErrorException e) {
            log.error("Error del cliente HTTP al llamar ML texto: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new MlServiceException("Error del servicio ML de texto: " + e.getMessage(), e);
        } catch (RestClientException e) {
            log.error("Error de conexión con servicio ML de texto: {}", e.getMessage());
            throw new MlServiceException("No se pudo conectar con el servicio ML de texto", e);
        } catch (Exception e) {
            log.error("Error inesperado al analizar texto: {}", e.getMessage(), e);
            throw new MlServiceException("Error inesperado en análisis de texto", e);
        }
    }

    /**
     * Verifica el estado del servicio ML de texto
     *
     * @return true si el servicio está disponible y el modelo está cargado
     */
    public boolean isServiceHealthy() {
        String url = mlTextoBaseUrl + "/api/ml/salud";

        try {
            ResponseEntity<HealthCheckResponse> response = restTemplate.getForEntity(
                    url,
                    HealthCheckResponse.class
            );

            return response.getStatusCode() == HttpStatus.OK &&
                   response.getBody() != null &&
                   response.getBody().isModelLoaded();

        } catch (Exception e) {
            log.warn("Servicio ML de texto no disponible: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Exception custom para errores del servicio ML
     */
    public static class MlServiceException extends Exception {
        public MlServiceException(String message) {
            super(message);
        }

        public MlServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * DTO interno para health check
     */
    @lombok.Data
    private static class HealthCheckResponse {
        private String status;

        @com.fasterxml.jackson.annotation.JsonProperty("model_loaded")
        private boolean modelLoaded;

        @com.fasterxml.jackson.annotation.JsonProperty("model_version")
        private String modelVersion;
    }
}
