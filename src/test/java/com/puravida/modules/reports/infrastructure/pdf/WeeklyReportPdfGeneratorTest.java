package com.puravida.modules.reports.infrastructure.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import com.puravida.modules.reports.application.dto.WeeklyReportOrdersSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSalesSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSourceSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class WeeklyReportPdfGeneratorTest {

    private final WeeklyReportPdfGenerator generator = new WeeklyReportPdfGenerator();

    @Test
    void generatesNonEmptyPdfForEmptyWeeklyData() {
        byte[] pdf = generator.generate(new WeeklyReportSummary(
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 12),
                LocalDateTime.of(2026, 7, 12, 18, 0),
                new WeeklyReportSalesSummary(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0,
                        0,
                        new WeeklyReportSourceSummary(0, BigDecimal.ZERO),
                        new WeeklyReportSourceSummary(0, BigDecimal.ZERO)
                ),
                new WeeklyReportOrdersSummary(0, 0, 0),
                List.of()
        ));

        assertThat(pdf).hasSizeGreaterThan(500);
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
}
