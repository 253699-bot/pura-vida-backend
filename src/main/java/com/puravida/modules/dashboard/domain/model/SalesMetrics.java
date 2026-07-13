package com.puravida.modules.dashboard.domain.model;

import java.math.BigDecimal;

public record SalesMetrics(
        BigDecimal activeTotal,
        BigDecimal cancelledTotal,
        long activeCount,
        long cancelledCount,
        SourceSalesMetrics manual,
        SourceSalesMetrics remote
) {
}
