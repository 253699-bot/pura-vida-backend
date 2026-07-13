package com.puravida.modules.sales.domain.model;

import com.puravida.modules.sales.domain.exception.SaleValidationException;
import java.util.Arrays;

public enum SaleSource {
    MANUAL_FONDA("manual_fonda"),
    REMOTA("remota");

    private final String databaseValue;

    SaleSource(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public static SaleSource fromDatabaseValue(String value) {
        return Arrays.stream(values())
                .filter(source -> source.databaseValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new SaleValidationException("Fuente de venta no soportada."));
    }
}
