package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.SalesMetrics;
import java.math.BigDecimal;

public record DashboardSalesResponse(
        BigDecimal totalActivo,
        BigDecimal totalAnulado,
        long cantidadActivas,
        long cantidadAnuladas,
        SourceSalesSummaryResponse manuales,
        SourceSalesSummaryResponse remotas
) {

    public static DashboardSalesResponse from(SalesMetrics metrics) {
        return new DashboardSalesResponse(
                metrics.activeTotal(),
                metrics.cancelledTotal(),
                metrics.activeCount(),
                metrics.cancelledCount(),
                SourceSalesSummaryResponse.from(metrics.manual()),
                SourceSalesSummaryResponse.from(metrics.remote())
        );
    }
}
