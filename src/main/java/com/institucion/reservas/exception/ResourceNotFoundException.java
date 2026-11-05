package com.institucion.reservas.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String error;

    public ResourceNotFoundException(String message) {
        this("NO_ENCONTRADO", message);
    }

    public ResourceNotFoundException(String error, String message) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
