package com.puravida.modules.orders.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectOrderRequest(
        @NotBlank(message = "El motivo de rechazo es obligatorio.")
        String motivoRechazo
) {
}
