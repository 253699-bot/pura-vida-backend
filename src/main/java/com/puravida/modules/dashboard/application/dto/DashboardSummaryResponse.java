package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.DashboardDateRange;
import com.puravida.modules.dashboard.domain.model.OrderMetrics;
import com.puravida.modules.dashboard.domain.model.SalesMetrics;
import java.time.LocalDate;

public record DashboardSummaryResponse(
        LocalDate from,
        LocalDate to,
        DashboardSalesResponse ventas,
        DashboardOrdersResponse pedidos
) {

    public static DashboardSummaryResponse from(
            DashboardDateRange range,
            SalesMetrics sales,
            OrderMetrics orders
    ) {
        return new DashboardSummaryResponse(
                range.from(),
                range.to(),
                DashboardSalesResponse.from(sales),
                DashboardOrdersResponse.from(orders)
        );
    }
}
