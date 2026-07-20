package com.puravida.modules.dashboard.domain.model;

import java.time.LocalDate;

public record PeakSalesHourMetric(LocalDate date, Integer hour, long count) {
}