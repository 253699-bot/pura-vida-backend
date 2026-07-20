package com.puravida.modules.orders.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptOrderRequest(
        @NotBlank(message = "El tiempo estimado de espera es obligatorio.")
        @Size(max = 100, message = "El tiempo estimado de espera no debe exceder 100 caracteres.")
        String tiempoEsperaEstimado
) {
}
