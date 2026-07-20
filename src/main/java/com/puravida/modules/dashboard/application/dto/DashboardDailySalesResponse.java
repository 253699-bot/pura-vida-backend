package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.DailySalesMetric;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardDailySalesResponse(LocalDate fecha, BigDecimal total, long cantidad) {

    public static DashboardDailySalesResponse from(DailySalesMetric metric) {
        return new DashboardDailySalesResponse(metric.date(), metric.total(), metric.count());
    }
}
