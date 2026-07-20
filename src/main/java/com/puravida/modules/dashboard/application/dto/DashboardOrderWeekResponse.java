package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.OrderWeekMetric;
import java.time.LocalDate;

public record DashboardOrderWeekResponse(int semana, LocalDate desde, LocalDate hasta, long cantidad) {

    public static DashboardOrderWeekResponse from(OrderWeekMetric metric) {
        return new DashboardOrderWeekResponse(metric.week(), metric.from(), metric.to(), metric.count());
    }
}