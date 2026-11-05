package com.institucion.reservas.dto;

import com.institucion.reservas.entity.EstadoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 20) String documento,
        @NotBlank @Email @Size(max = 100) String correo,
        @NotNull EstadoUsuario estado
) {
}
