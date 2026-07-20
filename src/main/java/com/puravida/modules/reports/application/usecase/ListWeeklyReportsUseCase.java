package com.puravida.modules.reports.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.reports.application.dto.WeeklyReportListItemResponse;
import com.puravida.modules.reports.application.port.in.ListWeeklyReportsPort;
import com.puravida.modules.reports.application.port.out.WeeklyReportRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListWeeklyReportsUseCase implements ListWeeklyReportsPort {

    private final WeeklyReportRepositoryPort repositoryPort;
    private final WeeklyReportAuthorizationService authorizationService;

    public ListWeeklyReportsUseCase(
            WeeklyReportRepositoryPort repositoryPort,
            WeeklyReportAuthorizationService authorizationService
    ) {
        this.repositoryPort = repositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyReportListItemResponse> list(AuthenticatedUser authenticatedUser) {
        authorizationService.requireEncargada(authenticatedUser);
        return repositoryPort.findAllOrderByGeneratedAtDesc().stream()
                .map(WeeklyReportListItemResponse::from)
                .toList();
    }
}
