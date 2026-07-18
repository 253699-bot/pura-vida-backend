package com.puravida.modules.users.application.dto;

import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequest(
        @Size(max = 150, message = "El nombre no debe exceder 150 caracteres.")
        String nombre,

        @Size(max = 20, message = "El telefono no debe exceder 20 caracteres.")
        String telefono,

        String correo,
        String rol,
        String password
) {
}
