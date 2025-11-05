package com.recepcion.recepcion.exception;

/**
 * Excepción lanzada cuando la petición es inválida (400)
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
