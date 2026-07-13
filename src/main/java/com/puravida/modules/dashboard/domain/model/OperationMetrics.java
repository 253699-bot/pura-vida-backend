package com.puravida.modules.dashboard.domain.model;

public record OperationMetrics(boolean configured, Boolean businessOpen, String closingReason) {

    public static OperationMetrics notConfigured() {
        return new OperationMetrics(false, null, null);
    }
}
