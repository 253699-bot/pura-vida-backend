package com.puravida.modules.reports.application.dto;

import java.time.LocalDate;

public record WeeklyReportPdf(byte[] content, LocalDate weekStart) {
}
