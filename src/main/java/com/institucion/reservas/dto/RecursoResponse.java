package com.institucion.reservas.dto;

import com.institucion.reservas.entity.EstadoRecurso;

public record RecursoResponse(
        Long id,
        String nombre,
        String descripcion,
        String tipo,
        Integer capacidad,
        EstadoRecurso estado
) {
}
