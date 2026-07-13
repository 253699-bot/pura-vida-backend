package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.TopDishMetric;
import java.math.BigDecimal;

public record TopDishResponse(
        Integer platilloId,
        String nombre,
        long cantidadVendida,
        BigDecimal totalGenerado
) {

    public static TopDishResponse from(TopDishMetric metric) {
        return new TopDishResponse(
                metric.dishId(),
                metric.dishName(),
                metric.quantity(),
                metric.total()
        );
    }
}
