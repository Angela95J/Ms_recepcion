package com.recepcion.recepcion.service.impl;

import com.recepcion.recepcion.client.MlImagenClient;
import com.recepcion.recepcion.client.MlTextoClient;
import com.recepcion.recepcion.dto.ml.AnalizarImagenResponse;
import com.recepcion.recepcion.dto.ml.AnalizarTextoResponse;
import com.recepcion.recepcion.entity.*;
import com.recepcion.recepcion.exception.ResourceNotFoundException;
import com.recepcion.recepcion.exception.ServiceException;
import com.recepcion.recepcion.repository.*;
import com.recepcion.recepcion.service.AnalisisMlOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalisisMlOrchestrationServiceImpl implements AnalisisMlOrchestrationService {

    private final IncidenteRepository incidenteRepository;
    private final MultimediaRepository multimediaRepository;
    private final AnalisisMlTextoRepository analisisTextoRepository;
    private final AnalisisMlImagenRepository analisisImagenRepository;
    private final MlTextoClient mlTextoClient;
    private final MlImagenClient mlImagenClient;

    @Value("${ml.texto.enabled:true}")
    private boolean mlTextoEnabled;

    @Value("${ml.imagen.enabled:true}")
    private boolean mlImagenEnabled;

    @Override
    @Async
    @Transactional
    public void analizarTextoAutomaticamente(UUID incidenteId) {
        log.info("===== MÉTODO ASÍNCRONO INICIADO: analizarTextoAutomaticamente =====");
        log.info("Thread actual: {}", Thread.currentThread().getName());
        log.info("Incidente ID: {}", incidenteId);

        if (!mlTextoEnabled) {
            log.info("Análisis de texto deshabilitado en configuración");
            return;
        }

        log.info("Iniciando análisis automático de texto para incidente: {}", incidenteId);

        try {
            // 1. Obtener incidente
            Incidente incidente = incidenteRepository.findById(incidenteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", incidenteId));

            // 2. Verificar que el servicio ML esté disponible
            if (!mlTextoClient.isServiceHealthy()) {
                log.error("Servicio ML de texto no disponible. Incidente {} no será analizado.", incidenteId);
                return;
            }

            // 3. Cambiar estado del incidente
            incidente.setEstadoIncidente(EstadoIncidente.EN_ANALISIS_TEXTO);
            incidenteRepository.save(incidente);

            // 4. Llamar al servicio ML
            AnalizarTextoResponse mlResponse = mlTextoClient.analizarTexto(
                    incidente.getDescripcionOriginal(),
                    incidenteId
            );

            // 5. Guardar resultado del análisis
            // Convertir List<String> a Map<String, Object> para palabras clave
            Map<String, Object> palabrasClaveMap = new java.util.HashMap<>();
            if (mlResponse.getPalabrasClaveCriticas() != null) {
                palabrasClaveMap.put("palabras", mlResponse.getPalabrasClaveCriticas());
            }

            AnalisisMlTexto analisis = AnalisisMlTexto.builder()
                    .incidente(incidente)
                    .textoAnalizado(incidente.getDescripcionOriginal())
                    .prioridadCalculada(mlResponse.getPrioridadCalculada())
                    .nivelGravedad(mlResponse.getNivelGravedad())
                    .tipoIncidentePredicho(mlResponse.getTipoIncidentePredicho())
                    .scoreConfianza(mlResponse.getScoreConfianza())
                    .palabrasClaveCriticas(palabrasClaveMap)
                    .categoriasDetectadas(mlResponse.getCategoriasDetectadas())
                    .entidadesMedicas(mlResponse.getEntidadesMedicas())
                    .probabilidadesCategorias(mlResponse.getProbabilidadesCategorias())
                    .modeloVersion(mlResponse.getModeloVersion())
                    .algoritmoUsado(mlResponse.getAlgoritmoUsado())
                    .tiempoProcesamientoMs(mlResponse.getTiempoProcesamientoMs())
                    .build();

            analisis = analisisTextoRepository.save(analisis);

            // 6. Actualizar incidente con resultado
            incidente.setAnalisisTexto(analisis);
            incidente.setPrioridadTexto(mlResponse.getPrioridadCalculada());
            incidente.setTipoIncidenteClasificado(mlResponse.getTipoIncidentePredicho());
            incidente.setEstadoIncidente(EstadoIncidente.ANALIZADO);

            // 7. Calcular prioridad final
            calcularPrioridadFinal(incidente);

            incidenteRepository.save(incidente);

            log.info("Análisis de texto completado exitosamente. Prioridad: {}",
                    mlResponse.getPrioridadCalculada());

        } catch (MlTextoClient.MlServiceException e) {
            log.error("Error en servicio ML de texto para incidente {}: {}",
                    incidenteId, e.getMessage());
            throw new ServiceException("Error al analizar texto del incidente", e);
        } catch (Exception e) {
            log.error("Error inesperado al analizar texto del incidente {}: {}",
                    incidenteId, e.getMessage(), e);
            throw new ServiceException("Error inesperado en análisis de texto", e);
        }
    }

    @Override
    @Async
    @Transactional
    public void analizarImagenAutomaticamente(UUID multimediaId) {
        if (!mlImagenEnabled) {
            log.info("Análisis de imagen deshabilitado en configuración");
            return;
        }

        log.info("Iniciando análisis automático de imagen para multimedia: {}", multimediaId);

        try {
            // 1. Obtener multimedia
            Multimedia multimedia = multimediaRepository.findById(multimediaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Multimedia", "id", multimediaId));

            Incidente incidente = multimedia.getIncidente();
            UUID incidenteId = incidente.getId();

            // 2. Verificar que sea una imagen
            if (multimedia.getTipoArchivo() != TipoArchivo.IMAGEN) {
                log.info("Archivo {} no es imagen, omitiendo análisis ML", multimediaId);
                return;
            }

            // 3. Verificar que el servicio ML esté disponible
            if (!mlImagenClient.isServiceHealthy()) {
                log.error("Servicio ML de imagen no disponible. Imagen {} no será analizada.", multimediaId);
                return;
            }

            // 4. Cambiar estado del incidente
            incidente.setEstadoIncidente(EstadoIncidente.EN_ANALISIS_IMAGEN);
            incidenteRepository.save(incidente);

            // 5. Llamar al servicio ML
            AnalizarImagenResponse mlResponse = mlImagenClient.analizarImagen(
                    multimedia.getUrlArchivo(),
                    multimediaId,
                    incidenteId
            );

            // 6. Guardar resultado del análisis
            // Convertir String a CalidadImagen enum
            CalidadImagen calidadImagenEnum = null;
            if (mlResponse.getCalidadImagen() != null) {
                try {
                    calidadImagenEnum = CalidadImagen.valueOf(mlResponse.getCalidadImagen().toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Calidad de imagen desconocida: {}", mlResponse.getCalidadImagen());
                    calidadImagenEnum = CalidadImagen.REGULAR; // Valor por defecto
                }
            }

            AnalisisMlImagen analisis = AnalisisMlImagen.builder()
                    .multimedia(multimedia)
                    .esImagenAccidente(mlResponse.getEsImagenAccidente())
                    .scoreVeracidad(mlResponse.getScoreVeracidad())
                    .tipoEscenaDetectada(mlResponse.getTipoEscenaDetectada())
                    .nivelGravedadVisual(mlResponse.getNivelGravedadVisual())
                    .elementosCriticosDetectados(mlResponse.getElementosCriticosDetectados())
                    .objetosDetectados(mlResponse.getObjetosDetectados())
                    .personasDetectadas(mlResponse.getPersonasDetectadas())
                    .vehiculosDetectados(mlResponse.getVehiculosDetectados())
                    .categoriasEscena(mlResponse.getCategoriasEscena())
                    .scoreConfianzaEscena(mlResponse.getScoreConfianzaEscena())
                    .esAnomalia(mlResponse.getEsAnomalia())
                    .scoreAnomalia(mlResponse.getScoreAnomalia())
                    .razonSospecha(mlResponse.getRazonSospecha())
                    .calidadImagen(calidadImagenEnum)
                    .resolucionImagen(mlResponse.getResolucionImagen())
                    .esImagenClara(mlResponse.getEsImagenClara())
                    .modeloVision(mlResponse.getModeloVision())
                    .modeloVeracidad(mlResponse.getModeloVeracidad())
                    .tiempoProcesamientoMs(mlResponse.getTiempoProcesamientoMs())
                    .build();

            analisis = analisisImagenRepository.save(analisis);

            // 7. Actualizar multimedia
            // Nota: La relación ya está establecida en analisis.multimedia
            multimedia.setAnalisisCompletado(true);
            multimediaRepository.save(multimedia);

            // 8. Actualizar incidente con resultado
            incidente.setPrioridadImagen(mlResponse.getNivelGravedadVisual());
            incidente.setScoreVeracidad(mlResponse.getScoreVeracidad());
            incidente.setEstadoIncidente(EstadoIncidente.ANALIZADO);

            // 9. Calcular prioridad final
            calcularPrioridadFinal(incidente);

            incidenteRepository.save(incidente);

            log.info("Análisis de imagen completado exitosamente. Severidad: {}, Veracidad: {}",
                    mlResponse.getNivelGravedadVisual(), mlResponse.getScoreVeracidad());

        } catch (MlImagenClient.MlServiceException e) {
            log.error("Error en servicio ML de imagen para multimedia {}: {}",
                    multimediaId, e.getMessage());
            throw new ServiceException("Error al analizar imagen", e);
        } catch (Exception e) {
            log.error("Error inesperado al analizar imagen {}: {}",
                    multimediaId, e.getMessage(), e);
            throw new ServiceException("Error inesperado en análisis de imagen", e);
        }
    }

    @Override
    @Transactional
    public void analizarIncidenteCompleto(UUID incidenteId) {
        log.info("Iniciando análisis completo del incidente: {}", incidenteId);

        // 1. Analizar texto
        analizarTextoAutomaticamente(incidenteId);

        // 2. Analizar todas las imágenes asociadas
        List<Multimedia> imagenes = multimediaRepository.findByIncidenteId(incidenteId);

        for (Multimedia imagen : imagenes) {
            if (imagen.getTipoArchivo() == TipoArchivo.IMAGEN
                && Boolean.TRUE.equals(imagen.getRequiereAnalisisMl())
                && !Boolean.TRUE.equals(imagen.getAnalisisCompletado())) {
                analizarImagenAutomaticamente(imagen.getId());
            }
        }

        log.info("Análisis completo del incidente {} iniciado", incidenteId);
    }

    /**
     * Calcula la prioridad final combinando análisis de texto e imagen
     */
    private void calcularPrioridadFinal(Incidente incidente) {
        Integer prioridadTexto = incidente.getPrioridadTexto();
        Integer prioridadImagen = incidente.getPrioridadImagen();

        if (prioridadTexto != null && prioridadImagen != null) {
            // Combinar ambas prioridades (60% texto + 40% imagen)
            int prioridadFinal = (int) Math.round((prioridadTexto * 0.6 + prioridadImagen * 0.4));
            incidente.setPrioridadFinal(prioridadFinal);
            log.info("Prioridad final calculada: {} (Texto: {}, Imagen: {})",
                    prioridadFinal, prioridadTexto, prioridadImagen);
        } else if (prioridadTexto != null) {
            // Solo hay análisis de texto
            incidente.setPrioridadFinal(prioridadTexto);
            log.info("Prioridad final basada solo en texto: {}", prioridadTexto);
        } else if (prioridadImagen != null) {
            // Solo hay análisis de imagen
            incidente.setPrioridadFinal(prioridadImagen);
            log.info("Prioridad final basada solo en imagen: {}", prioridadImagen);
        }
    }
}
