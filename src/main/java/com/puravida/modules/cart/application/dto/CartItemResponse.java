package com.puravida.modules.cart.application.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Integer id,
        Integer dishId,
        String nombre,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String imagenUrl
) {

    public CartItemResponse(
            Integer id,
            Integer dishId,
            String nombre,
            int cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {
        this(id, dishId, nombre, cantidad, precioUnitario, subtotal, null);
    }
}