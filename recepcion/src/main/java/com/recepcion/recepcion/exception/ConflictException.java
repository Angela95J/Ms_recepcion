package com.recepcion.recepcion.exception;

/**
 * Excepción lanzada cuando hay un conflicto (409)
 * Ejemplo: Duplicado, restricción de negocio violada
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
