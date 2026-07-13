package com.puravida.modules.reports.application.dto;

import com.puravida.modules.dashboard.application.dto.DashboardSummaryResponse;
import com.puravida.modules.dashboard.application.dto.TopDishesResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WeeklyReportSummary(
        LocalDate semanaInicio,
        LocalDate semanaFin,
        LocalDateTime generadoEn,
        WeeklyReportSalesSummary ventas,
        WeeklyReportOrdersSummary pedidos,
        List<WeeklyReportTopDish> topPlatillos
) {

    public static WeeklyReportSummary from(
            DashboardSummaryResponse summary,
            TopDishesResponse topDishes,
            LocalDateTime generatedAt
    ) {
        return new WeeklyReportSummary(
                summary.from(),
                summary.to(),
                generatedAt,
                WeeklyReportSalesSummary.from(summary.ventas()),
                WeeklyReportOrdersSummary.from(summary.pedidos()),
                topDishes.items().stream().map(WeeklyReportTopDish::from).toList()
        );
    }
}
