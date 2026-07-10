package com.puravida.modules.menu.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateTodayMenuRequest(
        @NotEmpty(message = "El menu debe incluir al menos un platillo.")
        List<@Valid UpdateTodayMenuItemRequest> items
) {
}
