package com.puravida.modules.reports.application.dto;

import com.puravida.modules.dashboard.application.dto.SourceSalesSummaryResponse;
import java.math.BigDecimal;

public record WeeklyReportSourceSummary(long cantidad, BigDecimal total) {

    public static WeeklyReportSourceSummary from(SourceSalesSummaryResponse source) {
        return new WeeklyReportSourceSummary(source.cantidad(), source.total());
    }
}
