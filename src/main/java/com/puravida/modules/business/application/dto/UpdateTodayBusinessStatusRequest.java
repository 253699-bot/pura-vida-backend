package com.puravida.modules.business.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateTodayBusinessStatusRequest(
        @NotNull(message = "El estado abierto es obligatorio.")
        Boolean abierto,
        String motivoCierre
) {
}
