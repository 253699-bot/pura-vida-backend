package com.puravida.modules.reports.application.usecase;

import com.puravida.modules.reports.domain.exception.WeeklyReportValidationException;
import com.puravida.modules.reports.domain.model.WeeklyReportRange;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportRangeResolver {

    public WeeklyReportRange resolve(String weekStart) {
        LocalDate start = isBlank(weekStart)
                ? LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                : parse(weekStart);
        return new WeeklyReportRange(start, start.plusDays(6));
    }

    private LocalDate parse(String weekStart) {
        try {
            return LocalDate.parse(weekStart.trim());
        } catch (DateTimeParseException exception) {
            throw new WeeklyReportValidationException("weekStart debe usar formato YYYY-MM-DD.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
