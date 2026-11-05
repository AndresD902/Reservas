package com.institucion.reservas.exception;

public class BusinessRuleException extends RuntimeException {

    private final String error;

    public BusinessRuleException(String message) {
        this("REGLA_DE_NEGOCIO", message);
    }

    public BusinessRuleException(String error, String message) {
        super(message);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
