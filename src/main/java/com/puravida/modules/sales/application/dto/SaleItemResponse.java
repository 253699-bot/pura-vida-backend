package com.puravida.modules.sales.application.dto;

import com.puravida.modules.sales.domain.model.ManualSaleLine;
import java.math.BigDecimal;

public record SaleItemResponse(
        Integer id,
        Integer menuItemId,
        Integer platilloId,
        String nombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {

    public static SaleItemResponse from(ManualSaleLine line) {
        return new SaleItemResponse(
                line.id(),
                line.menuItemId(),
                line.dishId(),
                line.dishName(),
                line.quantity(),
                line.unitPrice(),
                line.subtotal()
        );
    }
}
