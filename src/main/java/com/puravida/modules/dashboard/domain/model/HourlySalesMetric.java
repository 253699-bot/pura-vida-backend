package com.puravida.modules.dashboard.domain.model;

import java.math.BigDecimal;

public record HourlySalesMetric(int hour, BigDecimal total, long count) {
}
