package com.puravida.modules.reports.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.dashboard.application.dto.DashboardOrdersResponse;
import com.puravida.modules.dashboard.application.dto.DashboardSalesResponse;
import com.puravida.modules.dashboard.application.dto.DashboardSummaryResponse;
import com.puravida.modules.dashboard.application.dto.SourceSalesSummaryResponse;
import com.puravida.modules.dashboard.application.dto.TopDishResponse;
import com.puravida.modules.dashboard.application.dto.TopDishesResponse;
import com.puravida.modules.dashboard.application.port.in.GetDashboardSummaryPort;
import com.puravida.modules.dashboard.application.port.in.GetTopDishesPort;
import com.puravida.modules.users.domain.model.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetWeeklyReportSummaryUseCaseTest {

    @Mock
    private GetDashboardSummaryPort getDashboardSummaryPort;

    @Mock
    private GetTopDishesPort getTopDishesPort;

    @Mock
    private WeeklyReportRangeResolver rangeResolver;

    @InjectMocks
    private GetWeeklyReportSummaryUseCase useCase;

    @Test
    void reusesDashboardDataWithoutCountingCancelledSalesAsActive() {
        LocalDate from = LocalDate.of(2026, 7, 6);
        LocalDate to = LocalDate.of(2026, 7, 12);
        AuthenticatedUser encargada = encargada();
        var summary = new DashboardSummaryResponse(
                from,
                to,
                new DashboardSalesResponse(
                        new BigDecimal("300.00"),
                        new BigDecimal("50.00"),
                        3,
                        1,
                        new SourceSalesSummaryResponse(1, new BigDecimal("100.00")),
                        new SourceSalesSummaryResponse(2, new BigDecimal("200.00"))
                ),
                new DashboardOrdersResponse(2, 3, 1)
        );
        var top = new TopDishesResponse(from, to, List.of(
                new TopDishResponse(8, "Cochito", 12, new BigDecimal("960.00"))
        ));

        when(rangeResolver.resolve("2026-07-06"))
                .thenReturn(new com.puravida.modules.reports.domain.model.WeeklyReportRange(from, to));
        when(getDashboardSummaryPort.getSummary("2026-07-06", "2026-07-12", encargada)).thenReturn(summary);
        when(getTopDishesPort.getTopDishes("2026-07-06", "2026-07-12", encargada)).thenReturn(top);

        var report = useCase.getSummary("2026-07-06", encargada);

        assertThat(report.ventas().totalActivo()).isEqualByComparingTo("300.00");
        assertThat(report.ventas().totalAnulado()).isEqualByComparingTo("50.00");
        assertThat(report.ventas().manuales().cantidad()).isEqualTo(1);
        assertThat(report.ventas().remotas().cantidad()).isEqualTo(2);
        assertThat(report.topPlatillos()).hasSize(1);
        assertThat(report.pedidos().pendientes()).isEqualTo(2);
    }

    private AuthenticatedUser encargada() {
        return new AuthenticatedUser(4, "encargada@example.com", UserRole.ENCARGADA);
    }
}
