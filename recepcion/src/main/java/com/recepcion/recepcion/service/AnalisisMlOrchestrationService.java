package com.recepcion.recepcion.service;

import java.util.UUID;

/**
 * Servicio orquestador para análisis de Machine Learning
 * Coordina el análisis automático de texto e imágenes
 */
public interface AnalisisMlOrchestrationService {

    /**
     * Analiza automáticamente el texto del incidente al momento de su creación
     *
     * @param incidenteId ID del incidente
     */
    void analizarTextoAutomaticamente(UUID incidenteId);

    /**
     * Analiza automáticamente una imagen cuando se sube al incidente
     *
     * @param multimediaId ID del archivo multimedia
     */
    void analizarImagenAutomaticamente(UUID multimediaId);

    /**
     * Realiza análisis completo (texto + imágenes) de un incidente
     *
     * @param incidenteId ID del incidente
     */
    void analizarIncidenteCompleto(UUID incidenteId);
}
