package com.puravida.modules.orders.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectOrderRequest(
        @NotBlank(message = "La categoria de rechazo es obligatoria.")
        String categoriaRechazo,
        String motivoRechazo
) {
}
