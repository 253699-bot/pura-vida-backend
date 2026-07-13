package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.OperationMetrics;

public record DashboardOperationResponse(
        boolean configurado,
        Boolean negocioAbierto,
        String motivoCierre
) {

    public static DashboardOperationResponse from(OperationMetrics metrics) {
        return new DashboardOperationResponse(
                metrics.configured(),
                metrics.businessOpen(),
                metrics.closingReason()
        );
    }
}
