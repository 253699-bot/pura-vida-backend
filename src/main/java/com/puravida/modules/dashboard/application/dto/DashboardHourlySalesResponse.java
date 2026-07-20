package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.HourlySalesMetric;
import java.math.BigDecimal;

public record DashboardHourlySalesResponse(int hora, BigDecimal total, long cantidad) {

    public static DashboardHourlySalesResponse from(HourlySalesMetric metric) {
        return new DashboardHourlySalesResponse(metric.hour(), metric.total(), metric.count());
    }
}
