package com.puravida.modules.orders.application.dto;

import com.puravida.modules.orders.domain.model.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(
        Integer id,
        Integer menuItemId,
        Integer platilloId,
        String nombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String imagenUrl
) {

    public OrderItemResponse(
            Integer id,
            Integer menuItemId,
            Integer platilloId,
            String nombre,
            Integer cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {
        this(id, menuItemId, platilloId, nombre, cantidad, precioUnitario, subtotal, null);
    }

    public static OrderItemResponse from(OrderItem item) {
        return from(item, null);
    }

    public static OrderItemResponse from(OrderItem item, String imagenUrl) {
        return new OrderItemResponse(
                item.id(),
                item.menuItemId(),
                item.dishId(),
                item.nombrePlatillo(),
                item.cantidad(),
                item.precioUnitario(),
                item.subtotal(),
                imagenUrl
        );
    }
}