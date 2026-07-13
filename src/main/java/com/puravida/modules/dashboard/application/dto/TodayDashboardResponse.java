package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.OperationMetrics;
import com.puravida.modules.dashboard.domain.model.OrderMetrics;
import com.puravida.modules.dashboard.domain.model.SalesMetrics;
import java.time.LocalDate;

public record TodayDashboardResponse(
        LocalDate fecha,
        DashboardSalesResponse ventas,
        DashboardOrdersResponse pedidos,
        DashboardOperationResponse operacion
) {

    public static TodayDashboardResponse from(
            LocalDate date,
            SalesMetrics sales,
            OrderMetrics orders,
            OperationMetrics operation
    ) {
        return new TodayDashboardResponse(
                date,
                DashboardSalesResponse.from(sales),
                DashboardOrdersResponse.from(orders),
                DashboardOperationResponse.from(operation)
        );
    }
}
