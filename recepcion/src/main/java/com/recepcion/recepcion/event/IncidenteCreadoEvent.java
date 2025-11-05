package com.recepcion.recepcion.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class IncidenteCreadoEvent extends ApplicationEvent {
    private final UUID incidenteId;

    public IncidenteCreadoEvent(Object source, UUID incidenteId) {
        super(source);
        this.incidenteId = incidenteId;
    }
}
