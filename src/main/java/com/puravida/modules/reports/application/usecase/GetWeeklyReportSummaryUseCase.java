package com.puravida.modules.reports.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.dashboard.application.dto.DashboardSummaryResponse;
import com.puravida.modules.dashboard.application.dto.TopDishesResponse;
import com.puravida.modules.dashboard.application.port.in.GetDashboardSummaryPort;
import com.puravida.modules.dashboard.application.port.in.GetTopDishesPort;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import com.puravida.modules.reports.application.port.in.GetWeeklyReportSummaryPort;
import com.puravida.modules.reports.domain.model.WeeklyReportRange;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetWeeklyReportSummaryUseCase implements GetWeeklyReportSummaryPort {

    private final GetDashboardSummaryPort getDashboardSummaryPort;
    private final GetTopDishesPort getTopDishesPort;
    private final WeeklyReportRangeResolver rangeResolver;

    public GetWeeklyReportSummaryUseCase(
            GetDashboardSummaryPort getDashboardSummaryPort,
            GetTopDishesPort getTopDishesPort,
            WeeklyReportRangeResolver rangeResolver
    ) {
        this.getDashboardSummaryPort = getDashboardSummaryPort;
        this.getTopDishesPort = getTopDishesPort;
        this.rangeResolver = rangeResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyReportSummary getSummary(String weekStart, AuthenticatedUser authenticatedUser) {
        WeeklyReportRange range = rangeResolver.resolve(weekStart);
        String from = range.weekStart().toString();
        String to = range.weekEnd().toString();
        DashboardSummaryResponse summary = getDashboardSummaryPort.getSummary(from, to, authenticatedUser);
        TopDishesResponse topDishes = getTopDishesPort.getTopDishes(from, to, authenticatedUser);
        return WeeklyReportSummary.from(summary, topDishes, LocalDateTime.now());
    }
}
