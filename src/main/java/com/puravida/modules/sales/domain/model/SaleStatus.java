package com.puravida.modules.sales.domain.model;

import com.puravida.modules.sales.domain.exception.SaleValidationException;
import java.util.Arrays;

public enum SaleStatus {
    ACTIVA("activa"),
    ANULADA("anulada");

    private final String databaseValue;

    SaleStatus(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public static SaleStatus fromDatabaseValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.databaseValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new SaleValidationException("Estado de venta no soportado."));
    }
}
