package com.puravida.modules.reports.domain.model;

import java.time.LocalDate;

public record WeeklyReportRange(LocalDate weekStart, LocalDate weekEnd) {
}
