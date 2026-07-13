package com.puravida.modules.dashboard.domain.model;

import java.math.BigDecimal;

public record TopDishMetric(
        Integer dishId,
        String dishName,
        long quantity,
        BigDecimal total
) {
}
