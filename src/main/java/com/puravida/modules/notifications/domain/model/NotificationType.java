package com.puravida.modules.notifications.domain.model;

import java.util.Arrays;

public enum NotificationType {
    SISTEMA("sistema"),
    PEDIDO_CREADO("pedido_creado"),
    PEDIDO_ACEPTADO("pedido_aceptado"),
    PEDIDO_RECHAZADO("pedido_rechazado"),
    PEDIDO_CANCELADO("pedido_cancelado");

    private final String databaseValue;

    NotificationType(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public static NotificationType fromDatabaseValue(String value) {
        if (value == null || value.isBlank()) {
            return SISTEMA;
        }

        String normalizedValue = value.trim();
        return Arrays.stream(values())
                .filter(type -> type.databaseValue.equals(normalizedValue))
                .findFirst()
                .orElse(SISTEMA);
    }
}
