package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.PeriodSalesMetric;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardPeriodSalesResponse(
        String etiqueta,
        LocalDate desde,
        LocalDate hasta,
        BigDecimal total,
        long cantidad
) {

    public static DashboardPeriodSalesResponse from(PeriodSalesMetric metric) {
        return new DashboardPeriodSalesResponse(
                metric.label(),
                metric.from(),
                metric.to(),
                metric.total(),
                metric.count()
        );
    }
}