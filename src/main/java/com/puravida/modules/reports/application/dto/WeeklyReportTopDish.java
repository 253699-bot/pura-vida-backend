package com.puravida.modules.reports.application.dto;

import com.puravida.modules.dashboard.application.dto.TopDishResponse;
import java.math.BigDecimal;

public record WeeklyReportTopDish(
        Integer platilloId,
        String nombre,
        long cantidadVendida,
        BigDecimal totalGenerado
) {

    public static WeeklyReportTopDish from(TopDishResponse dish) {
        return new WeeklyReportTopDish(
                dish.platilloId(),
                dish.nombre(),
                dish.cantidadVendida(),
                dish.totalGenerado()
        );
    }
}
