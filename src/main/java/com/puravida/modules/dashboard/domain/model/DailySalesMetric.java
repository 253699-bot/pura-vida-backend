package com.puravida.modules.dashboard.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesMetric(LocalDate date, BigDecimal total, long count) {
}
