package com.puravida.modules.sales.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateManualSaleItemRequest(
        @NotNull(message = "El menuItemId es obligatorio.")
        Integer menuItemId,
        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser positiva.")
        Integer cantidad
) {
}
