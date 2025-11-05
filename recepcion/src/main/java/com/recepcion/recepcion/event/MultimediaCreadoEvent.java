package com.recepcion.recepcion.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class MultimediaCreadoEvent extends ApplicationEvent {
    private final UUID multimediaId;

    public MultimediaCreadoEvent(Object source, UUID multimediaId) {
        super(source);
        this.multimediaId = multimediaId;
    }
}
