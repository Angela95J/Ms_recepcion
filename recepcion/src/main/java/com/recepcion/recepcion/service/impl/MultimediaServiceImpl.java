package com.recepcion.recepcion.service.impl;

import com.recepcion.recepcion.dto.response.MultimediaResponse;
import com.recepcion.recepcion.entity.Incidente;
import com.recepcion.recepcion.entity.Multimedia;
import com.recepcion.recepcion.entity.TipoArchivo;
import com.recepcion.recepcion.event.MultimediaCreadoEvent;
import com.recepcion.recepcion.exception.BadRequestException;
import com.recepcion.recepcion.exception.ResourceNotFoundException;
import com.recepcion.recepcion.exception.ServiceException;
import com.recepcion.recepcion.mapper.MultimediaMapper;
import com.recepcion.recepcion.repository.IncidenteRepository;
import com.recepcion.recepcion.repository.MultimediaRepository;
import com.recepcion.recepcion.service.AnalisisMlOrchestrationService;
import com.recepcion.recepcion.service.MultimediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

//import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class MultimediaServiceImpl implements MultimediaService {

    private final MultimediaRepository multimediaRepository;
    private final IncidenteRepository incidenteRepository;
    private final MultimediaMapper multimediaMapper;
    private final AnalisisMlOrchestrationService analisisMlOrchestrationService;
    private final ApplicationEventPublisher eventPublisher;

    public MultimediaServiceImpl(
            MultimediaRepository multimediaRepository,
            IncidenteRepository incidenteRepository,
            MultimediaMapper multimediaMapper,
            @Lazy AnalisisMlOrchestrationService analisisMlOrchestrationService,
            ApplicationEventPublisher eventPublisher) {
        this.multimediaRepository = multimediaRepository;
        this.incidenteRepository = incidenteRepository;
        this.multimediaMapper = multimediaMapper;
        this.analisisMlOrchestrationService = analisisMlOrchestrationService;
        this.eventPublisher = eventPublisher;
    }

    @Value("${app.multimedia.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.multimedia.max-file-size:10485760}") // 10MB por defecto
    private Long maxFileSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/heic", "image/webp"
    );

    @Override
    public MultimediaResponse subirArchivo(UUID incidenteId, MultipartFile archivo, String descripcion, Boolean esPrincipal) {
        log.info("Subiendo archivo para incidente ID: {}", incidenteId);

        // Validar incidente existe
        Incidente incidente = incidenteRepository.findById(incidenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", incidenteId));

        // Validaciones del archivo
        validarArchivo(archivo);

        // Generar nombre único
        String nombreOriginal = archivo.getOriginalFilename();
        String extension = obtenerExtension(nombreOriginal);
        String nombreUnico = UUID.randomUUID().toString() + "." + extension;

        // Guardar archivo en disco
        String urlArchivo = guardarArchivo(archivo, nombreUnico);

        // Crear registro en BD
        Multimedia multimedia = Multimedia.builder()
                .incidente(incidente)
                .urlArchivo(urlArchivo)
                .nombreArchivo(nombreOriginal)
                .tipoArchivo(TipoArchivo.IMAGEN)
                .formatoArchivo(extension)
                .tamanoBytes(archivo.getSize())
                .descripcion(descripcion)
                .esPrincipal(esPrincipal != null ? esPrincipal : false)
                .requiereAnalisisMl(true)
                .analisisCompletado(false)
                .build();

        multimedia = multimediaRepository.save(multimedia);

        log.info("Archivo subido exitosamente con ID: {}", multimedia.getId());

        // Publicar evento para análisis ML de imagen si es imagen (se ejecutará después del COMMIT)
        if (multimedia.getTipoArchivo() == TipoArchivo.IMAGEN) {
            eventPublisher.publishEvent(new MultimediaCreadoEvent(this, multimedia.getId()));
            log.info("Evento MultimediaCreadoEvent publicado para multimedia: {}", multimedia.getId());
        }

        return multimediaMapper.toResponse(multimedia);
    }

    @Override
    @Transactional(readOnly = true)
    public MultimediaResponse buscarPorId(UUID id) {
        log.debug("Buscando multimedia por ID: {}", id);

        Multimedia multimedia = multimediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Multimedia", "id", id));

        return multimediaMapper.toResponse(multimedia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MultimediaResponse> listarPorIncidente(UUID incidenteId) {
        log.debug("Listando multimedia del incidente: {}", incidenteId);

        List<Multimedia> multimedia = multimediaRepository.findByIncidenteId(incidenteId);
        return multimediaMapper.toResponseList(multimedia);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] descargarArchivo(UUID id) {
        log.debug("Descargando archivo multimedia ID: {}", id);

        Multimedia multimedia = multimediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Multimedia", "id", id));

        try {
            Path filePath = Paths.get(multimedia.getUrlArchivo());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Error al leer archivo: {}", multimedia.getUrlArchivo(), e);
            throw new ServiceException("Error al descargar el archivo", e);
        }
    }

    @Override
    public void eliminar(UUID id) {
        log.info("Eliminando multimedia ID: {}", id);

        Multimedia multimedia = multimediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Multimedia", "id", id));

        // Eliminar archivo físico
        try {
            Files.deleteIfExists(Paths.get(multimedia.getUrlArchivo()));
            log.debug("Archivo físico eliminado: {}", multimedia.getUrlArchivo());
        } catch (IOException e) {
            log.warn("No se pudo eliminar archivo físico: {}", multimedia.getUrlArchivo(), e);
        }

        // Eliminar registro de BD
        multimediaRepository.delete(multimedia);
        log.info("Multimedia eliminado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MultimediaResponse> listarPendientesAnalisis() {
        log.debug("Listando multimedia pendiente de análisis ML");

        List<Multimedia> multimedia = multimediaRepository.findPendientesAnalisis();
        return multimediaMapper.toResponseList(multimedia);
    }

    @Override
    public void marcarComoAnalizado(UUID id) {
        log.info("Marcando multimedia ID: {} como analizado", id);

        Multimedia multimedia = multimediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Multimedia", "id", id));

        multimedia.setAnalisisCompletado(true);
        multimediaRepository.save(multimedia);

        log.info("Multimedia marcado como analizado: {}", id);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void validarArchivo(MultipartFile archivo) {
        // Validar archivo no vacío
        if (archivo.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }

        // Validar tamaño
        if (archivo.getSize() > maxFileSize) {
            throw new BadRequestException("El archivo excede el tamaño máximo permitido de " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Validar tipo MIME
        String contentType = archivo.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Tipo de archivo no permitido. Solo se aceptan imágenes JPG, PNG, HEIC, WEBP");
        }

        log.debug("Archivo validado correctamente: {} - {} bytes", archivo.getOriginalFilename(), archivo.getSize());
    }

    private String guardarArchivo(MultipartFile archivo, String nombreUnico) {
        try {
            // Crear directorio si no existe
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("Directorio de uploads creado: {}", uploadDir);
            }

            // Guardar archivo
            Path filePath = dirPath.resolve(nombreUnico);
            Files.write(filePath, archivo.getBytes());

            log.debug("Archivo guardado en: {}", filePath.toAbsolutePath());
            return filePath.toString();

        } catch (IOException e) {
            log.error("Error al guardar archivo", e);
            throw new ServiceException("Error al guardar el archivo", e);
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "jpg"; // Extensión por defecto
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
    }
}
