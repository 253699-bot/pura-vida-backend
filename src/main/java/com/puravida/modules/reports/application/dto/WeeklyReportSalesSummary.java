package com.puravida.modules.reports.application.dto;

import com.puravida.modules.dashboard.application.dto.DashboardSalesResponse;
import java.math.BigDecimal;

public record WeeklyReportSalesSummary(
        BigDecimal totalActivo,
        BigDecimal totalAnulado,
        long cantidadActivas,
        long cantidadAnuladas,
        WeeklyReportSourceSummary manuales,
        WeeklyReportSourceSummary remotas
) {

    public static WeeklyReportSalesSummary from(DashboardSalesResponse sales) {
        return new WeeklyReportSalesSummary(
                sales.totalActivo(),
                sales.totalAnulado(),
                sales.cantidadActivas(),
                sales.cantidadAnuladas(),
                WeeklyReportSourceSummary.from(sales.manuales()),
                WeeklyReportSourceSummary.from(sales.remotas())
        );
    }
}
