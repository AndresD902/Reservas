package com.institucion.reservas.dto;

import com.institucion.reservas.entity.EstadoReserva;

import java.time.LocalDateTime;

public record ReservaResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        Long recursoId,
        String recursoNombre,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        EstadoReserva estado,
        LocalDateTime fechaCreacion
) {
}
