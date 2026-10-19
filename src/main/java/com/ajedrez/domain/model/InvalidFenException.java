package com.ajedrez.domain.model;

public class InvalidFenException extends IllegalArgumentException {
    public InvalidFenException(String message) {
        super(message);
    }
}
