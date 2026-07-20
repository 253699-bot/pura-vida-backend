package com.puravida.modules.dashboard.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PeriodSalesMetric(String label, LocalDate from, LocalDate to, BigDecimal total, long count) {
}