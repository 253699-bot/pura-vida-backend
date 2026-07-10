package com.puravida.modules.menu.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateMenuItemAvailabilityRequest(
        @NotNull(message = "La disponibilidad es obligatoria.")
        Boolean disponible
) {
}
