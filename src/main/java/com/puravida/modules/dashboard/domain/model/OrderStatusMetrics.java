package com.puravida.modules.dashboard.domain.model;

public record OrderStatusMetrics(long pending, long accepted, long rejected, long finalized) {
}