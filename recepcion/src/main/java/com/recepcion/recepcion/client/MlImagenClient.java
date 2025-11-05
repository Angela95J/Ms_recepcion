package com.recepcion.recepcion.client;

import com.recepcion.recepcion.dto.ml.AnalizarImagenRequest;
import com.recepcion.recepcion.dto.ml.AnalizarImagenResponse;
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
public class MlImagenClient {

    private final RestTemplate restTemplate;
    private final String mlImagenBaseUrl;

    public MlImagenClient(
            RestTemplate restTemplate,
            @Value("${ml.imagen.base-url:http://localhost:8002}") String mlImagenBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.mlImagenBaseUrl = mlImagenBaseUrl;
    }

    /**
     * Analiza una imagen llamando al servicio ML de Python
     *
     * @param imagenPath Ruta de la imagen
     * @param multimediaId ID del multimedia
     * @param incidenteId ID del incidente (opcional)
     * @return Response con análisis completo
     * @throws MlServiceException si el servicio ML falla
     */
    public AnalizarImagenResponse analizarImagen(String imagenPath, UUID multimediaId, UUID incidenteId)
            throws MlServiceException {
        String url = mlImagenBaseUrl + "/api/ml/analizar-imagen";

        try {
            log.info("Llamando al servicio ML de imagen: {}", url);

            // Construir request
            AnalizarImagenRequest request = AnalizarImagenRequest.builder()
                    .imagenPath(imagenPath)
                    .multimediaId(multimediaId)
                    .incidenteId(incidenteId)
                    .build();

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<AnalizarImagenRequest> entity = new HttpEntity<>(request, headers);

            // Llamada HTTP POST
            ResponseEntity<AnalizarImagenResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    AnalizarImagenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Análisis de imagen completado. Es accidente: {}, Severidad: {}",
                        response.getBody().getEsImagenAccidente(),
                        response.getBody().getNivelGravedadVisual());
                return response.getBody();
            } else {
                throw new MlServiceException("Respuesta inválida del servicio ML de imagen");
            }

        } catch (HttpClientErrorException e) {
            log.error("Error del cliente HTTP al llamar ML imagen: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new MlServiceException("Error del servicio ML de imagen: " + e.getMessage(), e);
        } catch (RestClientException e) {
            log.error("Error de conexión con servicio ML de imagen: {}", e.getMessage());
            throw new MlServiceException("No se pudo conectar con el servicio ML de imagen", e);
        } catch (Exception e) {
            log.error("Error inesperado al analizar imagen: {}", e.getMessage(), e);
            throw new MlServiceException("Error inesperado en análisis de imagen", e);
        }
    }

    /**
     * Verifica el estado del servicio ML de imagen
     *
     * @return true si el servicio está disponible y el modelo está cargado
     */
    public boolean isServiceHealthy() {
        String url = mlImagenBaseUrl + "/api/ml/salud";

        try {
            ResponseEntity<HealthCheckResponse> response = restTemplate.getForEntity(
                    url,
                    HealthCheckResponse.class
            );

            return response.getStatusCode() == HttpStatus.OK &&
                   response.getBody() != null &&
                   response.getBody().isModelLoaded();

        } catch (Exception e) {
            log.warn("Servicio ML de imagen no disponible: {}", e.getMessage());
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
