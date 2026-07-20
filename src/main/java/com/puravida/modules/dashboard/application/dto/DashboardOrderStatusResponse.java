package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.OrderStatusMetrics;

public record DashboardOrderStatusResponse(long pendientes, long aceptados, long rechazados, long finalizados) {

    public static DashboardOrderStatusResponse from(OrderStatusMetrics metrics) {
        return new DashboardOrderStatusResponse(
                metrics.pending(),
                metrics.accepted(),
                metrics.rejected(),
                metrics.finalized()
        );
    }

    public static DashboardOrderStatusResponse empty() {
        return new DashboardOrderStatusResponse(0, 0, 0, 0);
    }
}