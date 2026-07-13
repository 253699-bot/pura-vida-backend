package com.puravida.modules.dashboard.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.dashboard.application.dto.TodayDashboardResponse;
import com.puravida.modules.dashboard.application.port.in.GetTodayDashboardPort;
import com.puravida.modules.dashboard.application.port.out.DashboardMetricsRepositoryPort;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetTodayDashboardUseCase implements GetTodayDashboardPort {

    private final DashboardMetricsRepositoryPort repositoryPort;
    private final DashboardAuthorizationService authorizationService;

    public GetTodayDashboardUseCase(
            DashboardMetricsRepositoryPort repositoryPort,
            DashboardAuthorizationService authorizationService
    ) {
        this.repositoryPort = repositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional(readOnly = true)
    public TodayDashboardResponse getToday(AuthenticatedUser authenticatedUser) {
        authorizationService.requireEncargada(authenticatedUser);
        LocalDate today = LocalDate.now();
        return TodayDashboardResponse.from(
                today,
                repositoryPort.aggregateSales(today, today),
                repositoryPort.aggregateOrders(today, today),
                repositoryPort.findOperation(today)
        );
    }
}
