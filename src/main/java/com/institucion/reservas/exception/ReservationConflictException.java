package com.institucion.reservas.exception;

public class ReservationConflictException extends RuntimeException {

    private final String error;

    public ReservationConflictException(String message) {
        this("RECURSO_NO_DISPONIBLE", message);
    }

    public ReservationConflictException(String error, String message) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
