package com.puravida.modules.reports.application.dto;

import com.puravida.modules.dashboard.application.dto.DashboardOrdersResponse;

public record WeeklyReportOrdersSummary(long pendientes, long aceptados, long rechazados, long cancelados) {

    public static WeeklyReportOrdersSummary from(DashboardOrdersResponse orders) {
        return new WeeklyReportOrdersSummary(
                orders.pendientes(),
                orders.aceptados(),
                orders.rechazados(),
                orders.cancelados()
        );
    }
}
