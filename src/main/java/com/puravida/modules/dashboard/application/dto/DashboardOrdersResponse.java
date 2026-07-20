package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.OrderMetrics;

public record DashboardOrdersResponse(long pendientes, long aceptados, long rechazados, long cancelados) {

    public static DashboardOrdersResponse from(OrderMetrics metrics) {
        return new DashboardOrdersResponse(
                metrics.pending(),
                metrics.accepted(),
                metrics.rejected(),
                metrics.cancelled()
        );
    }
}
