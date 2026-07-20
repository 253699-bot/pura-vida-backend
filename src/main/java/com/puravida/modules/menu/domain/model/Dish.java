package com.puravida.modules.menu.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Dish(
        Integer id,
        String nombre,
        String descripcion,
        String tipoPlatillo,
        BigDecimal precioBase,
        String imagenKey,
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
                null,
                true,
                LocalDateTime.now(),
                null
        );
    }

    public Dish deactivate() {
        return new Dish(
                id,
                nombre,
                descripcion,
                tipoPlatillo,
                precioBase,
                imagenKey,
                false,
                creadoEn,
                LocalDateTime.now()
        );
    }

    public Dish updateDetails(
            String nombre,
            String descripcion,
            String tipoPlatillo,
            BigDecimal precioBase
    ) {
        return new Dish(
                id,
                nombre,
                descripcion,
                tipoPlatillo,
                precioBase,
                imagenKey,
                activo,
                creadoEn,
                LocalDateTime.now()
        );
    }

    public Dish updateImage(String imagenKey) {
        return new Dish(
                id,
                nombre,
                descripcion,
                tipoPlatillo,
                precioBase,
                imagenKey,
                activo,
                creadoEn,
                LocalDateTime.now()
        );
    }
}