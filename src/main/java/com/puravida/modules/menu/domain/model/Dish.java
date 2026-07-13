package com.puravida.modules.menu.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Dish(
        Integer id,
        String nombre,
        String descripcion,
        String tipoPlatillo,
        BigDecimal precioBase,
        boolean activo,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {

    public static Dish create(
            String nombre,
            String descripcion,
            String tipoPlatillo,
            BigDecimal precioBase
    ) {
        return new Dish(
                null,
                nombre,
                descripcion,
                tipoPlatillo,
                precioBase,
                true,
                LocalDateTime.now(),
                null
        );
    }
}
