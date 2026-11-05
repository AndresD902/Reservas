package com.institucion.reservas.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservaRequest(
        @NotNull Long usuarioId,
        @NotNull Long recursoId,
        @NotNull LocalDateTime fechaInicio,
        @NotNull LocalDateTime fechaFin
) {
}
