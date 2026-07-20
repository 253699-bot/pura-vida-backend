package com.puravida.modules.menu.application.dto;

import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.shared.web.ApiPaths;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DishResponse(
        Integer id,
        String nombre,
        String descripcion,
        String tipoPlatillo,
        BigDecimal precioBase,
        String imagenUrl,
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
                publicImageUrl(dish),
                dish.activo(),
                dish.creadoEn(),
                dish.actualizadoEn()
        );
    }

    public static String publicImageUrl(Dish dish) {
        if (dish.id() == null || dish.imagenKey() == null || dish.imagenKey().isBlank()) {
            return null;
        }
        return ApiPaths.API_V1 + "/dishes/" + dish.id() + "/image";
    }
}