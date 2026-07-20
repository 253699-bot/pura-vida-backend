package com.puravida.modules.cart.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CartItem(
        Integer id,
        Integer userId,
        Integer dishId,
        int cantidad,
        BigDecimal precioUnitario,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {

    public static CartItem create(Integer userId, Integer dishId, int cantidad, BigDecimal precioUnitario) {
        return new CartItem(null, userId, dishId, cantidad, precioUnitario, LocalDateTime.now(), null);
    }

    public CartItem withCantidad(int nuevaCantidad) {
        return new CartItem(id, userId, dishId, nuevaCantidad, precioUnitario, creadoEn, LocalDateTime.now());
    }

    public CartItem withCantidadAndPrecio(int nuevaCantidad, BigDecimal nuevoPrecio) {
        return new CartItem(id, userId, dishId, nuevaCantidad, nuevoPrecio, creadoEn, LocalDateTime.now());
    }
}
