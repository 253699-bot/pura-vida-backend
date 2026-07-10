package com.puravida.modules.menu.application.dto;

import com.puravida.modules.menu.domain.model.DailyMenuItem;
import java.time.LocalDate;
import java.util.List;

public record TodayMenuResponse(
        boolean configured,
        LocalDate fecha,
        List<TodayMenuItemResponse> items
) {

    public static TodayMenuResponse configured(LocalDate fecha, List<DailyMenuItem> items) {
        return new TodayMenuResponse(
                true,
                fecha,
                items.stream().map(TodayMenuItemResponse::from).toList()
        );
    }

    public static TodayMenuResponse notConfigured(LocalDate fecha) {
        return new TodayMenuResponse(false, fecha, List.of());
    }
}
