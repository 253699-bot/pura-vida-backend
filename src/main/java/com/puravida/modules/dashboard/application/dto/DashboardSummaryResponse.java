package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.DailySalesMetric;
import com.puravida.modules.dashboard.domain.model.DashboardDateRange;
import com.puravida.modules.dashboard.domain.model.OrderMetrics;
import com.puravida.modules.dashboard.domain.model.OrderStatusMetrics;
import com.puravida.modules.dashboard.domain.model.OrderWeekMetric;
import com.puravida.modules.dashboard.domain.model.PeakSalesHourMetric;
import com.puravida.modules.dashboard.domain.model.PeriodSalesMetric;
import com.puravida.modules.dashboard.domain.model.SalesMetrics;
import java.time.LocalDate;
import java.util.List;

public record DashboardSummaryResponse(
        LocalDate from,
        LocalDate to,
        DashboardSalesResponse ventas,
        DashboardOrdersResponse pedidos,
        List<DashboardDailySalesResponse> ventasPorDia,
        List<DashboardPeriodSalesResponse> ventasPorPeriodo,
        List<DashboardPeakSalesHourResponse> horasPicoPorDia,
        List<DashboardOrderWeekResponse> pedidosPorSemanaMes,
        DashboardOrderStatusResponse pedidosPorEstado
) {

    public DashboardSummaryResponse(
            LocalDate from,
            LocalDate to,
            DashboardSalesResponse ventas,
            DashboardOrdersResponse pedidos,
            List<DashboardDailySalesResponse> ventasPorDia
    ) {
        this(
                from,
                to,
                ventas,
                pedidos,
                ventasPorDia,
                List.of(),
                List.of(),
                List.of(),
                DashboardOrderStatusResponse.empty()
        );
    }

    public static DashboardSummaryResponse from(
            DashboardDateRange range,
            SalesMetrics sales,
            OrderMetrics orders,
            List<DailySalesMetric> dailySales,
            List<PeriodSalesMetric> periodSales,
            List<PeakSalesHourMetric> peakHoursByDay,
            List<OrderWeekMetric> ordersByWeek,
            OrderStatusMetrics orderStatusMetrics
    ) {
        return new DashboardSummaryResponse(
                range.from(),
                range.to(),
                DashboardSalesResponse.from(sales),
                DashboardOrdersResponse.from(orders),
                dailySales.stream().map(DashboardDailySalesResponse::from).toList(),
                periodSales.stream().map(DashboardPeriodSalesResponse::from).toList(),
                peakHoursByDay.stream().map(DashboardPeakSalesHourResponse::from).toList(),
                ordersByWeek.stream().map(DashboardOrderWeekResponse::from).toList(),
                DashboardOrderStatusResponse.from(orderStatusMetrics)
        );
    }
}