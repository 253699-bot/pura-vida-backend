package com.puravida.modules.sales.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ManualSaleMenuItem(
        Integer id,
        LocalDate date,
        Integer dishId,
        String dishName,
        BigDecimal dailyPrice,
        boolean published,
        boolean dishActive,
        boolean available
) {
}
