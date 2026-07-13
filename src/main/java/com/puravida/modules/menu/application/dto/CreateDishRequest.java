package com.puravida.modules.menu.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateDishRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 150, message = "El nombre no debe exceder 150 caracteres.")
        String nombre,

        String descripcion,

        @NotBlank(message = "El tipo de platillo es obligatorio.")
        @Pattern(
                regexp = "platillo_fuerte|bebida|complemento|postre",
                message = "El tipo de platillo no es valido."
        )
        String tipoPlatillo,

        @NotNull(message = "El precio base es obligatorio.")
        @DecimalMin(value = "0.00", inclusive = false, message = "El precio base debe ser mayor a cero.")
        BigDecimal precioBase
) {
}
