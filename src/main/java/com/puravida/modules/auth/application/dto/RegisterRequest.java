package com.puravida.modules.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre es requerido.")
        @Size(max = 150, message = "El nombre no debe exceder 150 caracteres.")
        String nombre,

        @NotBlank(message = "El correo es requerido.")
        @Email(message = "El correo debe tener un formato valido.")
        @Size(max = 255, message = "El correo no debe exceder 255 caracteres.")
        String correo,

        @Size(max = 20, message = "El telefono no debe exceder 20 caracteres.")
        String telefono,

        @NotBlank(message = "La password es requerida.")
        @Size(min = 8, max = 100, message = "La password debe tener entre 8 y 100 caracteres.")
        String password
) {
}
