package com.recepcion.recepcion.event;

import com.recepcion.recepcion.service.AnalisisMlOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalisisMlEventListener {

    private final AnalisisMlOrchestrationService analisisMlOrchestrationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleIncidenteCreadoEvent(IncidenteCreadoEvent event) {
        log.info("Evento IncidenteCreadoEvent recibido después de COMMIT. Incidente ID: {}", event.getIncidenteId());
        try {
            analisisMlOrchestrationService.analizarTextoAutomaticamente(event.getIncidenteId());
        } catch (Exception e) {
            log.error("Error al procesar análisis de texto para incidente {}: {}",
                    event.getIncidenteId(), e.getMessage(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleMultimediaCreadoEvent(MultimediaCreadoEvent event) {
        log.info("Evento MultimediaCreadoEvent recibido después de COMMIT. Multimedia ID: {}", event.getMultimediaId());
        try {
            analisisMlOrchestrationService.analizarImagenAutomaticamente(event.getMultimediaId());
        } catch (Exception e) {
            log.error("Error al procesar análisis de imagen para multimedia {}: {}",
                    event.getMultimediaId(), e.getMessage(), e);
        }
    }
}
