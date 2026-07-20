package com.puravida.modules.business.domain.model;

public record ActiveBusinessOrderCounts(long pending, long accepted) {

    public long total() {
        return pending + accepted;
    }

    public boolean hasActiveOrders() {
        return total() > 0;
    }
}