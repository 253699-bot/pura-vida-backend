package com.puravida.modules.orders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderableMenuItem(
        Integer id,
        LocalDate fecha,
        Integer dishId,
        String nombrePlatillo,
        BigDecimal precioDia,
        boolean disponible,
        boolean dishActivo
) {
}
