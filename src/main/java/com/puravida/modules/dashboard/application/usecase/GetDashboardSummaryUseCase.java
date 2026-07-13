package com.puravida.modules.dashboard.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.dashboard.application.dto.DashboardSummaryResponse;
import com.puravida.modules.dashboard.application.port.in.GetDashboardSummaryPort;
import com.puravida.modules.dashboard.application.port.out.DashboardMetricsRepositoryPort;
import com.puravida.modules.dashboard.domain.model.DashboardDateRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetDashboardSummaryUseCase implements GetDashboardSummaryPort {

    private final DashboardMetricsRepositoryPort repositoryPort;
    private final DashboardAuthorizationService authorizationService;
    private final DashboardDateRangeResolver rangeResolver;

    public GetDashboardSummaryUseCase(
            DashboardMetricsRepositoryPort repositoryPort,
            DashboardAuthorizationService authorizationService,
            DashboardDateRangeResolver rangeResolver
    ) {
        this.repositoryPort = repositoryPort;
        this.authorizationService = authorizationService;
        this.rangeResolver = rangeResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(
            String from,
            String to,
            AuthenticatedUser authenticatedUser
    ) {
        authorizationService.requireEncargada(authenticatedUser);
        DashboardDateRange range = rangeResolver.resolve(from, to);
        return DashboardSummaryResponse.from(
                range,
                repositoryPort.aggregateSales(range.from(), range.to()),
                repositoryPort.aggregateOrders(range.from(), range.to())
        );
    }
}
