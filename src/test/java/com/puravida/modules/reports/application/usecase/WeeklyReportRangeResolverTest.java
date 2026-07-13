package com.puravida.modules.reports.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.puravida.modules.reports.domain.exception.WeeklyReportValidationException;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class WeeklyReportRangeResolverTest {

    private final WeeklyReportRangeResolver resolver = new WeeklyReportRangeResolver();

    @Test
    void createsInclusiveSevenDayRange() {
        var range = resolver.resolve("2026-07-06");

        assertThat(range.weekStart().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(ChronoUnit.DAYS.between(range.weekStart(), range.weekEnd())).isEqualTo(6);
    }

    @Test
    void defaultsToCurrentWeekStart() {
        var range = resolver.resolve(null);

        assertThat(range.weekStart().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void rejectsInvalidWeekStart() {
        assertThatThrownBy(() -> resolver.resolve("07-06-2026"))
                .isInstanceOf(WeeklyReportValidationException.class);
    }
}
