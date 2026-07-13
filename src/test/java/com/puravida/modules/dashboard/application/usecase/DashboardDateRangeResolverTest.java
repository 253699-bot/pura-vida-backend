package com.puravida.modules.dashboard.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.puravida.modules.dashboard.domain.exception.DashboardValidationException;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class DashboardDateRangeResolverTest {

    private final DashboardDateRangeResolver resolver = new DashboardDateRangeResolver();

    @Test
    void usesSevenDayDefaultRange() {
        var range = resolver.resolve(null, null);

        assertThat(ChronoUnit.DAYS.between(range.from(), range.to())).isEqualTo(6);
    }

    @Test
    void rejectsInvertedRange() {
        assertThatThrownBy(() -> resolver.resolve("2026-07-31", "2026-07-01"))
                .isInstanceOf(DashboardValidationException.class);
    }

    @Test
    void rejectsRangesLongerThanOneYear() {
        assertThatThrownBy(() -> resolver.resolve("2025-01-01", "2026-07-01"))
                .isInstanceOf(DashboardValidationException.class);
    }
}
