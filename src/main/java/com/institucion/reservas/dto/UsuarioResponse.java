package com.institucion.reservas.dto;

import com.institucion.reservas.entity.EstadoUsuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String documento,
        String correo,
        EstadoUsuario estado
) {
}
