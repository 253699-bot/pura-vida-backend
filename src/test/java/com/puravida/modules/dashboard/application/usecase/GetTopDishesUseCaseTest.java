package com.puravida.modules.dashboard.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.dashboard.application.port.out.DashboardMetricsRepositoryPort;
import com.puravida.modules.dashboard.domain.model.DashboardDateRange;
import com.puravida.modules.dashboard.domain.model.TopDishMetric;
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
class GetTopDishesUseCaseTest {

    @Mock
    private DashboardMetricsRepositoryPort repositoryPort;

    @Mock
    private DashboardAuthorizationService authorizationService;

    @Mock
    private DashboardDateRangeResolver rangeResolver;

    @InjectMocks
    private GetTopDishesUseCase useCase;

    @Test
    void returnsTopRemoteDishesForRange() {
        var from = LocalDate.of(2026, 7, 1);
        var to = LocalDate.of(2026, 7, 11);
        when(rangeResolver.resolve("2026-07-01", "2026-07-11"))
                .thenReturn(new DashboardDateRange(from, to));
        when(repositoryPort.findTopDishes(from, to, 10)).thenReturn(List.of(
                new TopDishMetric(8, "Cochito", 12, new BigDecimal("960.00"))
        ));

        var response = useCase.getTopDishes("2026-07-01", "2026-07-11", encargada());

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).nombre()).isEqualTo("Cochito");
        assertThat(response.items().get(0).cantidadVendida()).isEqualTo(12);
    }

    private AuthenticatedUser encargada() {
        return new AuthenticatedUser(4, "encargada@example.com", UserRole.ENCARGADA);
    }
}
