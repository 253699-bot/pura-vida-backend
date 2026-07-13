package com.puravida.modules.sales.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelSaleRequest(
        @NotBlank(message = "El motivo de anulacion es obligatorio.")
        String motivo
) {
}
