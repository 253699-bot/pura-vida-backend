package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.SourceSalesMetrics;
import java.math.BigDecimal;

public record SourceSalesSummaryResponse(long cantidad, BigDecimal total) {

    public static SourceSalesSummaryResponse from(SourceSalesMetrics metrics) {
        return new SourceSalesSummaryResponse(metrics.count(), metrics.total());
    }
}
