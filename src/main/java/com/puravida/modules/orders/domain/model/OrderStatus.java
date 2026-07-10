package com.puravida.modules.orders.domain.model;

import com.puravida.modules.orders.domain.exception.OrderValidationException;
import java.util.Arrays;

public enum OrderStatus {
    PENDIENTE("pendiente"),
    ACEPTADO("aceptado"),
    RECHAZADO("rechazado"),
    CANCELADO("cancelado");

    private final String databaseValue;

    OrderStatus(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public static OrderStatus fromDatabaseValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.databaseValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new OrderValidationException("Estado de pedido no soportado."));
    }
}
