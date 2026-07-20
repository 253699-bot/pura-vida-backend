package com.puravida.modules.dashboard.domain.model;

import java.time.LocalDate;

public record OrderWeekMetric(int week, LocalDate from, LocalDate to, long count) {
}