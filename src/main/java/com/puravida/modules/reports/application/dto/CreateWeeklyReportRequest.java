package com.puravida.modules.reports.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateWeeklyReportRequest(
        @NotBlank(message = "weekStart es obligatorio.")
        String weekStart
) {
}
