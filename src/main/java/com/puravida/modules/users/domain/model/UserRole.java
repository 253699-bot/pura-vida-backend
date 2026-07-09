package com.puravida.modules.users.domain.model;

import java.util.Arrays;

public enum UserRole {
    CLIENTE("cliente"),
    ENCARGADA("encargada");

    private final String databaseValue;

    UserRole(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public static UserRole fromDatabaseValue(String value) {
        return Arrays.stream(values())
                .filter(role -> role.databaseValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rol de usuario no soportado: " + value));
    }
}
