package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.PeakSalesHourMetric;
import java.time.LocalDate;

public record DashboardPeakSalesHourResponse(LocalDate fecha, Integer hora, long cantidad) {

    public static DashboardPeakSalesHourResponse from(PeakSalesHourMetric metric) {
        return new DashboardPeakSalesHourResponse(metric.date(), metric.hour(), metric.count());
    }
}