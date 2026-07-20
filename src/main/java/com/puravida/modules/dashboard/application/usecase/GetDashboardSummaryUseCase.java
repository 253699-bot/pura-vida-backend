package com.puravida.modules.dashboard.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.dashboard.application.dto.DashboardSummaryResponse;
import com.puravida.modules.dashboard.application.port.in.GetDashboardSummaryPort;
import com.puravida.modules.dashboard.application.port.out.DashboardMetricsRepositoryPort;
import com.puravida.modules.dashboard.domain.model.DailySalesMetric;
import com.puravida.modules.dashboard.domain.model.DashboardDateRange;
import com.puravida.modules.dashboard.domain.model.PeriodSalesMetric;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
        List<DailySalesMetric> dailySales = repositoryPort.findSalesByDay(range.from(), range.to());
        return DashboardSummaryResponse.from(
                range,
                repositoryPort.aggregateSales(range.from(), range.to()),
                repositoryPort.aggregateOrders(range.from(), range.to()),
                dailySales,
                groupSales(range, dailySales),
                repositoryPort.findPeakSalesHourByDay(range.from(), range.to()),
                repositoryPort.findOrdersByWeekOfMonth(range.from(), range.to()),
                repositoryPort.aggregateOrderStatuses(range.from(), range.to())
        );
    }

    private List<PeriodSalesMetric> groupSales(DashboardDateRange range, List<DailySalesMetric> dailySales) {
        long days = ChronoUnit.DAYS.between(range.from(), range.to()) + 1;
        if (days <= 31) {
            return dailySales.stream()
                    .map(metric -> new PeriodSalesMetric(
                            metric.date().toString(),
                            metric.date(),
                            metric.date(),
                            metric.total(),
                            metric.count()
                    ))
                    .toList();
        }
        if (days <= 120) {
            return groupByFixedWeeks(range, dailySales);
        }
        return groupByMonth(range, dailySales);
    }

    private List<PeriodSalesMetric> groupByFixedWeeks(
            DashboardDateRange range,
            List<DailySalesMetric> dailySales
    ) {
        List<PeriodSalesMetric> grouped = new ArrayList<>();
        LocalDate cursor = range.from();
        while (!cursor.isAfter(range.to())) {
            LocalDate groupFrom = cursor;
            LocalDate groupTo = cursor.plusDays(6).isAfter(range.to()) ? range.to() : cursor.plusDays(6);
            BigDecimal total = totalBetween(dailySales, groupFrom, groupTo);
            long count = countBetween(dailySales, groupFrom, groupTo);
            grouped.add(new PeriodSalesMetric(groupFrom + " - " + groupTo, groupFrom, groupTo, total, count));
            cursor = groupTo.plusDays(1);
        }
        return grouped;
    }

    private List<PeriodSalesMetric> groupByMonth(DashboardDateRange range, List<DailySalesMetric> dailySales) {
        List<PeriodSalesMetric> grouped = new ArrayList<>();
        LocalDate cursor = range.from().withDayOfMonth(1);
        while (!cursor.isAfter(range.to())) {
            LocalDate groupFrom = cursor.isBefore(range.from()) ? range.from() : cursor;
            LocalDate monthEnd = cursor.plusMonths(1).minusDays(1);
            LocalDate groupTo = monthEnd.isAfter(range.to()) ? range.to() : monthEnd;
            BigDecimal total = totalBetween(dailySales, groupFrom, groupTo);
            long count = countBetween(dailySales, groupFrom, groupTo);
            grouped.add(new PeriodSalesMetric(cursor.getYear() + "-" + twoDigits(cursor.getMonthValue()), groupFrom, groupTo, total, count));
            cursor = cursor.plusMonths(1);
        }
        return grouped;
    }

    private BigDecimal totalBetween(List<DailySalesMetric> metrics, LocalDate from, LocalDate to) {
        return metrics.stream()
                .filter(metric -> !metric.date().isBefore(from) && !metric.date().isAfter(to))
                .map(DailySalesMetric::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long countBetween(List<DailySalesMetric> metrics, LocalDate from, LocalDate to) {
        return metrics.stream()
                .filter(metric -> !metric.date().isBefore(from) && !metric.date().isAfter(to))
                .mapToLong(DailySalesMetric::count)
                .sum();
    }

    private String twoDigits(int value) {
        return value < 10 ? "0" + value : Integer.toString(value);
    }
}