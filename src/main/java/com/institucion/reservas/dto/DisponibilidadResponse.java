package com.institucion.reservas.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DisponibilidadResponse(
        Long recursoId,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        boolean disponible,
        List<ReservaResponse> reservasSolapadas
) {
}
