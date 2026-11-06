package com.institucion.reservas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "el correo es obligatorio")
        @Email(message = "el correo no tiene un formato válido")
        String correo,

        @NotBlank(message = "la contraseña es obligatoria")
        String password
) {
}
