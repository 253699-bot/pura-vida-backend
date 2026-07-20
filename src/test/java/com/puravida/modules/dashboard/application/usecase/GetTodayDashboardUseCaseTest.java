package com.puravida.modules.dashboard.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.dashboard.application.port.out.DashboardMetricsRepositoryPort;
import com.puravida.modules.dashboard.domain.model.HourlySalesMetric;
import com.puravida.modules.dashboard.domain.model.OperationMetrics;
import com.puravida.modules.dashboard.domain.model.OrderMetrics;
import com.puravida.modules.dashboard.domain.model.SalesMetrics;
import com.puravida.modules.dashboard.domain.model.SourceSalesMetrics;
import com.puravida.modules.users.domain.model.UserRole;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTodayDashboardUseCaseTest {

    @Mock
    private DashboardMetricsRepositoryPort repositoryPort;

    @Mock
    private DashboardAuthorizationService authorizationService;

    @InjectMocks
    private GetTodayDashboardUseCase useCase;

    @Test
    void separatesActiveCancelledAndSourceMetrics() {
        when(repositoryPort.aggregateSales(any(), any())).thenReturn(new SalesMetrics(
                new BigDecimal("300.00"),
                new BigDecimal("50.00"),
                3,
                1,
                new SourceSalesMetrics(1, new BigDecimal("100.00")),
                new SourceSalesMetrics(2, new BigDecimal("200.00"))
        ));
        when(repositoryPort.aggregateOrders(any(), any())).thenReturn(new OrderMetrics(2, 3, 1, 4));
        when(repositoryPort.findOperation(any())).thenReturn(new OperationMetrics(true, true, null));
        when(repositoryPort.findSalesByHour(any())).thenReturn(List.of(
                new HourlySalesMetric(12, new BigDecimal("300.00"), 3)
        ));

        var response = useCase.getToday(encargada());

        assertThat(response.ventas().totalActivo()).isEqualByComparingTo("300.00");
        assertThat(response.ventas().totalAnulado()).isEqualByComparingTo("50.00");
        assertThat(response.ventas().manuales().total()).isEqualByComparingTo("100.00");
        assertThat(response.ventas().remotas().total()).isEqualByComparingTo("200.00");
        assertThat(response.pedidos().pendientes()).isEqualTo(2);
        assertThat(response.pedidos().cancelados()).isEqualTo(4);
        assertThat(response.ventasPorHora()).hasSize(1);
        assertThat(response.operacion().negocioAbierto()).isTrue();
    }

    @Test
    void returnsZerosWhenThereAreNoSales() {
        when(repositoryPort.aggregateSales(any(), any())).thenReturn(new SalesMetrics(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                new SourceSalesMetrics(0, BigDecimal.ZERO),
                new SourceSalesMetrics(0, BigDecimal.ZERO)
        ));
        when(repositoryPort.aggregateOrders(any(), any())).thenReturn(new OrderMetrics(0, 0, 0, 0));
        when(repositoryPort.findOperation(any())).thenReturn(OperationMetrics.notConfigured());
        when(repositoryPort.findSalesByHour(any())).thenReturn(List.of());

        var response = useCase.getToday(encargada());

        assertThat(response.ventas().cantidadActivas()).isZero();
        assertThat(response.ventas().totalActivo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.operacion().configurado()).isFalse();
    }

    private AuthenticatedUser encargada() {
        return new AuthenticatedUser(4, "encargada@example.com", UserRole.ENCARGADA);
    }
}
