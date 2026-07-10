package com.puravida.modules.menu.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateTodayMenuItemRequest(
        @NotNull(message = "El platillo es obligatorio.")
        @Positive(message = "El platillo debe ser valido.")
        Integer platilloId
) {
}
