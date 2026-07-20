package com.puravida.modules.reports.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.reports.application.dto.CreateWeeklyReportRequest;
import com.puravida.modules.reports.application.dto.WeeklyReportOrdersSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSalesSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSourceSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportTopDish;
import com.puravida.modules.reports.application.port.in.GetWeeklyReportSummaryPort;
import com.puravida.modules.reports.application.port.out.WeeklyReportRepositoryPort;
import com.puravida.modules.reports.application.port.out.WeeklyReportSnapshotCodecPort;
import com.puravida.modules.reports.domain.exception.WeeklyReportValidationException;
import com.puravida.modules.reports.domain.model.StoredWeeklyReport;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateWeeklyReportUseCaseTest {

    @Mock WeeklyReportRepositoryPort repositoryPort;
    @Mock WeeklyReportSnapshotCodecPort snapshotCodecPort;
    @Mock GetWeeklyReportSummaryPort getSummaryPort;
    @Mock WeeklyReportAuthorizationService authorizationService;
    private CreateWeeklyReportUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateWeeklyReportUseCase(
                repositoryPort, snapshotCodecPort, getSummaryPort,
                new WeeklyReportRangeResolver(), authorizationService
        );
    }

    @Test
    void createsANewSnapshotForEveryGenerationEvenWhenWeekMatches() {
        AuthenticatedUser authenticated = authenticated();
        WeeklyReportSummary firstSummary = summary(new BigDecimal("300.00"), 2, 8);
        WeeklyReportSummary secondSummary = summary(new BigDecimal("450.00"), 3, 12);
        AtomicInteger nextId = new AtomicInteger(21);

        when(authorizationService.requireEncargada(authenticated)).thenReturn(actor());
        when(getSummaryPort.getSummary("2026-07-06", authenticated)).thenReturn(firstSummary, secondSummary);
        when(snapshotCodecPort.serialize(firstSummary)).thenReturn("snapshot-one");
        when(snapshotCodecPort.serialize(secondSummary)).thenReturn("snapshot-two");
        when(repositoryPort.create(any())).thenAnswer(invocation -> withId(invocation.getArgument(0), nextId.getAndIncrement()));

        var firstResponse = useCase.create(new CreateWeeklyReportRequest("2026-07-06"), authenticated);
        var secondResponse = useCase.create(new CreateWeeklyReportRequest("2026-07-06"), authenticated);

        assertThat(firstResponse.id()).isEqualTo(21);
        assertThat(secondResponse.id()).isEqualTo(22);
        ArgumentCaptor<StoredWeeklyReport> captor = ArgumentCaptor.forClass(StoredWeeklyReport.class);
        verify(repositoryPort, times(2)).create(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(StoredWeeklyReport::resumenJson)
                .containsExactly("snapshot-one", "snapshot-two");
        assertThat(captor.getAllValues())
                .extracting(StoredWeeklyReport::totalIngresos)
                .containsExactly(new BigDecimal("300.00"), new BigDecimal("450.00"));
    }

    @Test
    void persistsSupportedMetricsAndImmutableJsonSnapshot() {
        AuthenticatedUser authenticated = authenticated();
        WeeklyReportSummary summary = summary(new BigDecimal("300.00"), 2, 8);

        when(authorizationService.requireEncargada(authenticated)).thenReturn(actor());
        when(getSummaryPort.getSummary("2026-07-06", authenticated)).thenReturn(summary);
        when(snapshotCodecPort.serialize(summary)).thenReturn("snapshot-json");
        when(repositoryPort.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.create(new CreateWeeklyReportRequest("2026-07-06"), authenticated);

        ArgumentCaptor<StoredWeeklyReport> captor = ArgumentCaptor.forClass(StoredWeeklyReport.class);
        verify(repositoryPort).create(captor.capture());
        StoredWeeklyReport candidate = captor.getValue();
        assertThat(candidate.totalPedidosApp()).isEqualTo(2);
        assertThat(candidate.totalIngresos()).isEqualByComparingTo("300.00");
        assertThat(candidate.platilloMasVendidoId()).isEqualTo(8);
        assertThat(candidate.diaMayorDemanda()).isNull();
        assertThat(candidate.rutaArchivo()).isNull();
        assertThat(candidate.resumenJson()).isEqualTo("snapshot-json");
        assertThat(candidate.versionFormato()).isEqualTo(1);
        assertThat(candidate.generadoPor()).isEqualTo(4);
    }

    @Test
    void rejectsPersistingAWeekThatDoesNotStartOnMonday() {
        when(authorizationService.requireEncargada(authenticated())).thenReturn(actor());

        assertThatThrownBy(() -> useCase.create(
                new CreateWeeklyReportRequest("2026-07-07"), authenticated()
        ))
                .isInstanceOf(WeeklyReportValidationException.class)
                .hasMessage("weekStart debe corresponder a un lunes.");

        verify(repositoryPort, never()).create(any());
    }

    private WeeklyReportSummary summary(BigDecimal totalActivo, long remoteSales, int topDishId) {
        return new WeeklyReportSummary(
                LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12),
                LocalDateTime.of(2026, 7, 12, 18, 0),
                new WeeklyReportSalesSummary(
                        totalActivo, BigDecimal.ZERO, remoteSales + 1, 0,
                        new WeeklyReportSourceSummary(1, new BigDecimal("100.00")),
                        new WeeklyReportSourceSummary(remoteSales, totalActivo.subtract(new BigDecimal("100.00")))
                ),
                new WeeklyReportOrdersSummary(1, 2, 0, 0),
                List.of(new WeeklyReportTopDish(topDishId, "Cochito", 12, new BigDecimal("960.00")))
        );
    }

    private StoredWeeklyReport withId(StoredWeeklyReport report, Integer id) {
        return new StoredWeeklyReport(
                id, report.semanaInicio(), report.semanaFin(), report.totalPedidosApp(),
                report.totalIngresos(), report.platilloMasVendidoId(), report.diaMayorDemanda(),
                report.rutaArchivo(), report.resumenJson(), report.versionFormato(), report.generadoPor(),
                report.generadoEn()
        );
    }

    private AuthenticatedUser authenticated() {
        return new AuthenticatedUser(4, "admin@example.com", UserRole.ENCARGADA);
    }

    private User actor() {
        return new User(
                4, "Encargada", "admin@example.com", null, "hash", UserRole.ENCARGADA,
                null, true, true, LocalDateTime.of(2026, 7, 1, 10, 0), null
        );
    }
}