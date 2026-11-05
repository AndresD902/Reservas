package com.institucion.reservas.exception;

public record ErrorResponseDto(
        int status,
        String error,
        String message
) {
}
