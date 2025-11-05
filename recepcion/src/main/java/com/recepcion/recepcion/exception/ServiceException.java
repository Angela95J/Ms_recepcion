package com.recepcion.recepcion.exception;

/**
 * Excepción genérica de servicio (500)
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
