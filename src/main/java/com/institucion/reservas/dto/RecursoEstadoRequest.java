package com.institucion.reservas.dto;

import com.institucion.reservas.entity.EstadoRecurso;
import jakarta.validation.constraints.NotNull;

public record RecursoEstadoRequest(
        @NotNull EstadoRecurso estado
) {
}
