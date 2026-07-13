package com.puravida.modules.sales.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateManualSaleRequest(
        @NotNull(message = "El total es obligatorio.")
        @DecimalMin(value = "0.00", inclusive = false, message = "El total debe ser mayor a cero.")
        @Digits(integer = 8, fraction = 2, message = "El total debe tener hasta 8 enteros y 2 decimales.")
        BigDecimal total,
        String observaciones
) {
}
