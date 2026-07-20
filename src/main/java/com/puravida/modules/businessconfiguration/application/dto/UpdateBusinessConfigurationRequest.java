package com.puravida.modules.businessconfiguration.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateBusinessConfigurationRequest(
        @Size(max = 150, message = "El nombre de la fonda no debe exceder 150 caracteres.")
        String nombreFonda,
        @Size(max = 2000, message = "La direccion no debe exceder 2000 caracteres.")
        String direccion,
        @Size(max = 2000, message = "Los horarios no deben exceder 2000 caracteres.")
        String horarios,
        @Size(max = 20, message = "El telefono no debe exceder 20 caracteres.")
        @Pattern(regexp = "^[0-9+(). -]*$", message = "El telefono contiene caracteres no permitidos.")
        String telefono,
        @Email(message = "El correo debe tener un formato valido.")
        @Size(max = 255, message = "El correo no debe exceder 255 caracteres.")
        String correo
) {
}
