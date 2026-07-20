package com.puravida.modules.menu.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateTodayMenuRequest(
        @NotNull(message = "La lista de platillos del menu es obligatoria.")
        List<@Valid UpdateTodayMenuItemRequest> items
) {
}
