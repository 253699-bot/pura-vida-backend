package com.puravida.modules.reports.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.reports.application.dto.WeeklyReportOrdersSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSalesSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSourceSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import com.puravida.modules.reports.application.port.in.GetWeeklyReportSummaryPort;
import com.puravida.modules.reports.application.port.out.WeeklyReportPdfGeneratorPort;
import com.puravida.modules.users.domain.model.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateWeeklyReportPdfUseCaseTest {

    @Mock
    private GetWeeklyReportSummaryPort getWeeklyReportSummaryPort;

    @Mock
    private WeeklyReportPdfGeneratorPort pdfGeneratorPort;

    @InjectMocks
    private GenerateWeeklyReportPdfUseCase useCase;

    @Test
    void generatesPdfFromWeeklySummary() {
        WeeklyReportSummary summary = emptySummary();
        byte[] bytes = "%PDF-test".getBytes();
        AuthenticatedUser encargada = new AuthenticatedUser(4, "encargada@example.com", UserRole.ENCARGADA);
        when(getWeeklyReportSummaryPort.getSummary("2026-07-06", encargada)).thenReturn(summary);
        when(pdfGeneratorPort.generate(summary)).thenReturn(bytes);

        var report = useCase.generate("2026-07-06", encargada);

        assertThat(report.weekStart()).isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(report.content()).isEqualTo(bytes);
    }

    private WeeklyReportSummary emptySummary() {
        return new WeeklyReportSummary(
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
                new WeeklyReportOrdersSummary(0, 0, 0, 0),
                List.of()
        );
    }
}
