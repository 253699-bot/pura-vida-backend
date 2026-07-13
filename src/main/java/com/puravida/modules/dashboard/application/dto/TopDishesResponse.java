package com.puravida.modules.dashboard.application.dto;

import com.puravida.modules.dashboard.domain.model.DashboardDateRange;
import com.puravida.modules.dashboard.domain.model.TopDishMetric;
import java.time.LocalDate;
import java.util.List;

public record TopDishesResponse(LocalDate from, LocalDate to, List<TopDishResponse> items) {

    public static TopDishesResponse from(DashboardDateRange range, List<TopDishMetric> metrics) {
        return new TopDishesResponse(
                range.from(),
                range.to(),
                metrics.stream().map(TopDishResponse::from).toList()
        );
    }
}
