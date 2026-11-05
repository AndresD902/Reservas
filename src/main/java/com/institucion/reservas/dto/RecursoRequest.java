package com.institucion.reservas.dto;

import com.institucion.reservas.entity.EstadoRecurso;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecursoRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 255) String descripcion,
        @NotBlank @Size(max = 50) String tipo,
        @NotNull @Min(1) Integer capacidad,
        @NotNull EstadoRecurso estado
) {
}
