package com.institucion.reservas.dto;

import com.institucion.reservas.entity.EstadoUsuario;
import jakarta.validation.constraints.NotNull;

public record UsuarioEstadoRequest(
        @NotNull EstadoUsuario estado
) {
}
