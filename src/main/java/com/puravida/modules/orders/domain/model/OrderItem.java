package com.puravida.modules.orders.domain.model;

import java.math.BigDecimal;

public record OrderItem(
        Integer id,
        Integer orderId,
        Integer dishId,
        Integer menuItemId,
        String nombrePlatillo,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {

    public static OrderItem create(Integer orderId, OrderableMenuItem menuItem, Integer cantidad) {
        BigDecimal subtotal = menuItem.precioDia().multiply(BigDecimal.valueOf(cantidad));
        return new OrderItem(
                null,
                orderId,
                menuItem.dishId(),
                menuItem.id(),
                menuItem.nombrePlatillo(),
                cantidad,
                menuItem.precioDia(),
                subtotal
        );
    }
}
