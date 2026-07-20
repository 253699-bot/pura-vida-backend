package com.puravida.modules.sales.domain.model;

import java.math.BigDecimal;

public record ManualSaleLine(
        Integer id,
        Integer saleId,
        Integer dishId,
        Integer menuItemId,
        String dishName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {

    public static ManualSaleLine create(ManualSaleMenuItem menuItem, Integer quantity) {
        BigDecimal subtotal = menuItem.dailyPrice().multiply(BigDecimal.valueOf(quantity.longValue()));
        return new ManualSaleLine(
                null,
                null,
                menuItem.dishId(),
                menuItem.id(),
                menuItem.dishName(),
                quantity,
                menuItem.dailyPrice(),
                subtotal
        );
    }

    public ManualSaleLine assignTo(Integer saleId) {
        return new ManualSaleLine(
                id,
                saleId,
                dishId,
                menuItemId,
                dishName,
                quantity,
                unitPrice,
                subtotal
        );
    }
}
