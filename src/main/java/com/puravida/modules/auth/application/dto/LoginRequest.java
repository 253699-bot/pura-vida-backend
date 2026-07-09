package com.puravida.modules.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El correo es requerido.")
        @Email(message = "El correo debe tener un formato valido.")
        String correo,

        @NotBlank(message = "La password es requerida.")
        String password
) {
}
