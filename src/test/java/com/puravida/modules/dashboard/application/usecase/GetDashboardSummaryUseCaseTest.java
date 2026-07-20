package com.puravida.modules.dashboard.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.dashboard.application.port.out.DashboardMetricsRepositoryPort;
import com.puravida.modules.dashboard.domain.model.DailySalesMetric;
import com.puravida.modules.dashboard.domain.model.DashboardDateRange;
import com.puravida.modules.dashboard.domain.model.OrderMetrics;
import com.puravida.modules.dashboard.domain.model.OrderStatusMetrics;
import com.puravida.modules.dashboard.domain.model.SalesMetrics;
import com.puravida.modules.dashboard.domain.model.SourceSalesMetrics;
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
class GetDashboardSummaryUseCaseTest {

    @Mock
    private DashboardMetricsRepositoryPort repositoryPort;

    @Mock
    private DashboardAuthorizationService authorizationService;

    @Mock
    private DashboardDateRangeResolver rangeResolver;

    @InjectMocks
    private GetDashboardSummaryUseCase useCase;

    @Test
    void returnsAggregatesForResolvedRange() {
        var from = LocalDate.of(2026, 7, 1);
        var to = LocalDate.of(2026, 7, 11);
        when(rangeResolver.resolve("2026-07-01", "2026-07-11"))
                .thenReturn(new DashboardDateRange(from, to));
        when(repositoryPort.aggregateSales(from, to)).thenReturn(new SalesMetrics(
                new BigDecimal("500.00"),
                BigDecimal.ZERO,
                4,
                0,
                new SourceSalesMetrics(2, new BigDecimal("150.00")),
                new SourceSalesMetrics(2, new BigDecimal("350.00"))
        ));
        when(repositoryPort.aggregateOrders(from, to)).thenReturn(new OrderMetrics(1, 2, 3, 4));
        when(repositoryPort.findSalesByDay(from, to)).thenReturn(List.of(
                new DailySalesMetric(from, new BigDecimal("120.00"), 2)
        ));
        when(repositoryPort.aggregateOrderStatuses(from, to))
                .thenReturn(new OrderStatusMetrics(1, 2, 3, 5));
        when(repositoryPort.findPeakSalesHourByDay(from, to)).thenReturn(List.of());
        when(repositoryPort.findOrdersByWeekOfMonth(from, to)).thenReturn(List.of());

        var response = useCase.getSummary("2026-07-01", "2026-07-11", encargada());

        assertThat(response.from()).isEqualTo(from);
        assertThat(response.to()).isEqualTo(to);
        assertThat(response.ventas().totalActivo()).isEqualByComparingTo("500.00");
        assertThat(response.ventas().manuales().cantidad()).isEqualTo(2);
        assertThat(response.pedidos().rechazados()).isEqualTo(3);
        assertThat(response.pedidos().cancelados()).isEqualTo(4);
        assertThat(response.ventasPorDia()).hasSize(1);
        assertThat(response.ventasPorPeriodo()).isNotEmpty();
        assertThat(response.pedidosPorEstado().pendientes()).isEqualTo(1);
        assertThat(response.pedidosPorEstado().finalizados()).isEqualTo(5);
    }

    private AuthenticatedUser encargada() {
        return new AuthenticatedUser(4, "encargada@example.com", UserRole.ENCARGADA);
    }
}