package com.ajedrez.infrastructure.persistence;

/** Error de acceso a datos, sin checked exceptions que contaminen las capas superiores. */
public class PersistenceException extends RuntimeException {
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
