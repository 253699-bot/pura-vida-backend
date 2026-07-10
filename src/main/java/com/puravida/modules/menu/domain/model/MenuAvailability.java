package com.puravida.modules.menu.domain.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record MenuAvailability(
        Integer id,
        Integer menuItemId,
        boolean disponible,
        LocalTime horaPublicacion,
        LocalTime horaAgotado,
        LocalDateTime actualizadoEn
) {
}
