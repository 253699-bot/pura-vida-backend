package com.puravida.modules.dashboard.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.dashboard.application.dto.TopDishesResponse;
import com.puravida.modules.dashboard.application.port.in.GetTopDishesPort;
import com.puravida.modules.dashboard.application.port.out.DashboardMetricsRepositoryPort;
import com.puravida.modules.dashboard.domain.model.DashboardDateRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetTopDishesUseCase implements GetTopDishesPort {

    private static final int TOP_LIMIT = 10;

    private final DashboardMetricsRepositoryPort repositoryPort;
    private final DashboardAuthorizationService authorizationService;
    private final DashboardDateRangeResolver rangeResolver;

    public GetTopDishesUseCase(
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
    public TopDishesResponse getTopDishes(String from, String to, AuthenticatedUser authenticatedUser) {
        authorizationService.requireEncargada(authenticatedUser);
        DashboardDateRange range = rangeResolver.resolve(from, to);
        return TopDishesResponse.from(
                range,
                repositoryPort.findTopDishes(range.from(), range.to(), TOP_LIMIT)
        );
    }
}
