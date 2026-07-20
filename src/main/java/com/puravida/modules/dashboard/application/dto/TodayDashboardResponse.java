package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.HourlySalesMetric;
import com.puravida.modules.dashboard.domain.model.OperationMetrics;
import com.puravida.modules.dashboard.domain.model.OrderMetrics;
import com.puravida.modules.dashboard.domain.model.SalesMetrics;
import java.time.LocalDate;
import java.util.List;

public record TodayDashboardResponse(
        LocalDate fecha,
        DashboardSalesResponse ventas,
        DashboardOrdersResponse pedidos,
        DashboardOperationResponse operacion,
        List<DashboardHourlySalesResponse> ventasPorHora
) {

    public static TodayDashboardResponse from(
            LocalDate date,
            SalesMetrics sales,
            OrderMetrics orders,
            OperationMetrics operation,
            List<HourlySalesMetric> hourlySales
    ) {
        return new TodayDashboardResponse(
                date,
                DashboardSalesResponse.from(sales),
                DashboardOrdersResponse.from(orders),
                DashboardOperationResponse.from(operation),
                hourlySales.stream().map(DashboardHourlySalesResponse::from).toList()
        );
    }
}
