package com.puravida.modules.reports.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.reports.application.dto.WeeklyReportOrdersSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSalesSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSourceSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import com.puravida.modules.reports.application.port.out.WeeklyReportPdfGeneratorPort;
import com.puravida.modules.reports.application.port.out.WeeklyReportRepositoryPort;
import com.puravida.modules.reports.application.port.out.WeeklyReportSnapshotCodecPort;
import com.puravida.modules.reports.domain.model.StoredWeeklyReport;
import com.puravida.modules.users.domain.model.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateStoredWeeklyReportPdfUseCaseTest {

    @Mock WeeklyReportRepositoryPort repositoryPort;
    @Mock WeeklyReportSnapshotCodecPort snapshotCodecPort;
    @Mock WeeklyReportPdfGeneratorPort pdfGeneratorPort;
    @Mock WeeklyReportAuthorizationService authorizationService;
    @InjectMocks GenerateStoredWeeklyReportPdfUseCase useCase;

    @Test
    void regeneratesPdfFromStoredSnapshotWithoutLiveMetrics() {
        AuthenticatedUser actor = new AuthenticatedUser(4, "admin@example.com", UserRole.ENCARGADA);
        WeeklyReportSummary summary = summary();
        StoredWeeklyReport stored = new StoredWeeklyReport(
                12, summary.semanaInicio(), summary.semanaFin(), 0, BigDecimal.ZERO,
                null, null, null, "{}", 1, 4, summary.generadoEn()
        );
        byte[] pdf = "%PDF-stored".getBytes();
        when(repositoryPort.findById(12)).thenReturn(Optional.of(stored));
        when(snapshotCodecPort.deserialize("{}", 1)).thenReturn(summary);
        when(pdfGeneratorPort.generate(summary)).thenReturn(pdf);

        var result = useCase.generate(12, actor);

        assertThat(result.weekStart()).isEqualTo(summary.semanaInicio());
        assertThat(result.content()).isEqualTo(pdf);
    }

    private WeeklyReportSummary summary() {
        return new WeeklyReportSummary(
                LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12),
                LocalDateTime.of(2026, 7, 12, 18, 0),
                new WeeklyReportSalesSummary(
                        BigDecimal.ZERO, BigDecimal.ZERO, 0, 0,
                        new WeeklyReportSourceSummary(0, BigDecimal.ZERO),
                        new WeeklyReportSourceSummary(0, BigDecimal.ZERO)
                ),
                new WeeklyReportOrdersSummary(0, 0, 0, 0), List.of()
        );
    }
}

