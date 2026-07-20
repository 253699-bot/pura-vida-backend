package com.puravida.modules.menu.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailyMenuItem(
        Integer id,
        LocalDate fecha,
        Dish dish,
        BigDecimal precioDia,
        Integer creadoPor,
        LocalDateTime creadoEn,
        boolean publicado,
        MenuAvailability availability
) {

    public boolean disponible() {
        return availability == null || availability.disponible();
    }
}
