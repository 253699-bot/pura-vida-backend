package com.puravida.modules.menu.application.dto;

import com.puravida.modules.menu.domain.model.Dish;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DishResponse(
        Integer id,
        String nombre,
        String descripcion,
        String tipoPlatillo,
        BigDecimal precioBase,
        boolean activo,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {

    public static DishResponse from(Dish dish) {
        return new DishResponse(
                dish.id(),
                dish.nombre(),
                dish.descripcion(),
                dish.tipoPlatillo(),
                dish.precioBase(),
                dish.activo(),
                dish.creadoEn(),
                dish.actualizadoEn()
        );
    }
}
