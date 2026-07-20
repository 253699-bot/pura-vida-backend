package com.puravida.modules.reports.infrastructure.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.puravida.modules.reports.application.dto.WeeklyReportOrdersSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSalesSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSourceSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import com.puravida.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class JacksonWeeklyReportSnapshotCodecTest {

    private final JacksonWeeklyReportSnapshotCodec codec = new JacksonWeeklyReportSnapshotCodec(
            new ObjectMapper().registerModule(new JavaTimeModule())
    );

    @Test
    void roundTripsVersionOneAndRejectsUnsupportedVersion() {
        WeeklyReportSummary summary = new WeeklyReportSummary(
                LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12),
                LocalDateTime.of(2026, 7, 12, 18, 0),
                new WeeklyReportSalesSummary(
                        BigDecimal.TEN, BigDecimal.ZERO, 1, 0,
                        new WeeklyReportSourceSummary(1, BigDecimal.TEN),
                        new WeeklyReportSourceSummary(0, BigDecimal.ZERO)
                ),
                new WeeklyReportOrdersSummary(0, 1, 0, 0), List.of()
        );

        assertThat(codec.deserialize(codec.serialize(summary), 1)).isEqualTo(summary);
        assertThatThrownBy(() -> codec.deserialize("{}", 2))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> codec.deserialize(null, 1))
                .isInstanceOf(ConflictException.class);
    }
}

