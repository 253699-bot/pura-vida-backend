package com.puravida.modules.menu.application.dto;

import com.puravida.modules.menu.domain.model.DailyMenuItem;
import java.math.BigDecimal;

public record TodayMenuItemResponse(
        Integer id,
        Integer platilloId,
        String nombre,
        String descripcion,
        String tipoPlatillo,
        BigDecimal precio,
        boolean disponible
) {

    public static TodayMenuItemResponse from(DailyMenuItem item) {
        return new TodayMenuItemResponse(
                item.id(),
                item.dish().id(),
                item.dish().nombre(),
                item.dish().descripcion(),
                item.dish().tipoPlatillo(),
                item.precioDia(),
                item.disponible()
        );
    }
}
