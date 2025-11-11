package com.recepcion.recepcion.controller;

import com.recepcion.recepcion.dto.response.MultimediaResponse;
import com.recepcion.recepcion.service.MultimediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
///import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para gestión de archivos multimedia
 * Base URL: /api/multimedia
 */
@Slf4j
@RestController
@RequestMapping("/multimedia")
@RequiredArgsConstructor
@Tag(name = "Multimedia", description = "Gestión de archivos multimedia")
public class MultimediaController {

    private final MultimediaService multimediaService;

    /**
     * Subir archivo multimedia a un incidente
     * POST /api/multimedia/incidente/{incidenteId}/subir
     */
    @Operation(summary = "Subir archivo multimedia",
               description = "Sube una imagen, audio o video asociado a un incidente")
    @PostMapping(value = "/incidente/{incidenteId}/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MultimediaResponse> subirArchivo(
            @Parameter(description = "ID del incidente") @PathVariable UUID incidenteId,
            @Parameter(description = "Archivo a subir", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("archivo") MultipartFile archivo,
            @Parameter(description = "Descripción del archivo") @RequestParam(value = "descripcion", required = false) String descripcion,
            @Parameter(description = "Si es la imagen principal") @RequestParam(value = "esPrincipal", defaultValue = "false") Boolean esPrincipal) {
        log.info("Subiendo archivo para incidente ID: {} - Archivo: {} - Tamaño: {} bytes",
                 incidenteId, archivo.getOriginalFilename(), archivo.getSize());
        MultimediaResponse response = multimediaService.subirArchivo(incidenteId, archivo, descripcion, esPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener información de archivo multimedia por ID
     * GET /api/multimedia/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MultimediaResponse> buscarPorId(@PathVariable UUID id) {
        log.info("Buscando multimedia por ID: {}", id);
        MultimediaResponse response = multimediaService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar multimedia de un incidente
     * GET /api/multimedia/incidente/{incidenteId}
     */
    @GetMapping("/incidente/{incidenteId}")
    public ResponseEntity<List<MultimediaResponse>> listarPorIncidente(@PathVariable UUID incidenteId) {
        log.info("Listando multimedia del incidente ID: {}", incidenteId);
        List<MultimediaResponse> response = multimediaService.listarPorIncidente(incidenteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Ver/Servir archivo multimedia (para n8n, ML, frontend)
     * GET /api/multimedia/{id}/ver
     * Sirve el archivo inline para visualización directa
     */
    @Operation(summary = "Ver archivo multimedia",
               description = "Sirve el archivo para visualización directa en navegador o consumo por servicios ML",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Archivo encontrado",
                       content = @Content(mediaType = "image/*"))
               })
    @GetMapping("/{id}/ver")
    public ResponseEntity<byte[]> verArchivo(@Parameter(description = "ID del archivo") @PathVariable UUID id) {
        log.info("Sirviendo archivo multimedia ID: {}", id);

        // Obtener información del archivo
        MultimediaResponse info = multimediaService.buscarPorId(id);

        // Obtener contenido del archivo
        byte[] contenido = multimediaService.descargarArchivo(id);

        // Determinar el tipo de contenido basado en la extensión
        String contentType = determinarContentType(info.getNombreArchivo());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + info.getNombreArchivo() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(contenido);
    }

    /**
     * Descargar archivo multimedia
     * GET /api/multimedia/{id}/descargar
     */
    @Operation(summary = "Descargar archivo multimedia",
               description = "Descarga el archivo como attachment",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Archivo descargado",
                       content = @Content(mediaType = "application/octet-stream"))
               })
    @GetMapping("/{id}/descargar")
    public ResponseEntity<byte[]> descargarArchivo(@Parameter(description = "ID del archivo") @PathVariable UUID id) {
        log.info("Descargando archivo multimedia ID: {}", id);

        // Obtener información del archivo
        MultimediaResponse info = multimediaService.buscarPorId(id);

        // Obtener contenido del archivo
        byte[] contenido = multimediaService.descargarArchivo(id);

        // Determinar el tipo de contenido basado en la extensión
        String contentType = determinarContentType(info.getNombreArchivo());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + info.getNombreArchivo() + "\"")
                .body(contenido);
    }

    /**
     * Eliminar archivo multimedia
     * DELETE /api/multimedia/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        log.info("Eliminando multimedia ID: {}", id);
        multimediaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Listar multimedia pendiente de análisis
     * GET /api/multimedia/pendientes-analisis
     */
    @GetMapping("/pendientes-analisis")
    public ResponseEntity<List<MultimediaResponse>> listarPendientesAnalisis() {
        log.info("Listando multimedia pendiente de análisis");
        List<MultimediaResponse> response = multimediaService.listarPendientesAnalisis();
        return ResponseEntity.ok(response);
    }

    /**
     * Marcar multimedia como analizado
     * PATCH /api/multimedia/{id}/marcar-analizado
     */
    @PatchMapping("/{id}/marcar-analizado")
    public ResponseEntity<Void> marcarComoAnalizado(@PathVariable UUID id) {
        log.info("Marcando multimedia ID: {} como analizado", id);
        multimediaService.marcarComoAnalizado(id);
        return ResponseEntity.ok().build();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Determina el tipo de contenido basado en la extensión del archivo
     */
    private String determinarContentType(String nombreArchivo) {
        if (nombreArchivo == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "webm" -> "video/webm";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "pdf" -> "application/pdf";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}
