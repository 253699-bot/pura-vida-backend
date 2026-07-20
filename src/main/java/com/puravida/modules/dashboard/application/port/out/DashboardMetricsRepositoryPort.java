package com.puravida.modules.dashboard.application.port.out;

import com.puravida.modules.dashboard.domain.model.DailySalesMetric;
import com.puravida.modules.dashboard.domain.model.HourlySalesMetric;
import com.puravida.modules.dashboard.domain.model.OperationMetrics;
import com.puravida.modules.dashboard.domain.model.OrderMetrics;
import com.puravida.modules.dashboard.domain.model.OrderStatusMetrics;
import com.puravida.modules.dashboard.domain.model.OrderWeekMetric;
import com.puravida.modules.dashboard.domain.model.PeakSalesHourMetric;
import com.puravida.modules.dashboard.domain.model.SalesMetrics;
import com.puravida.modules.dashboard.domain.model.TopDishMetric;
import java.time.LocalDate;
import java.util.List;

public interface DashboardMetricsRepositoryPort {

    SalesMetrics aggregateSales(LocalDate from, LocalDate to);

    OrderMetrics aggregateOrders(LocalDate from, LocalDate to);

    OrderStatusMetrics aggregateOrderStatuses(LocalDate from, LocalDate to);

    OperationMetrics findOperation(LocalDate date);

    List<DailySalesMetric> findSalesByDay(LocalDate from, LocalDate to);

    List<PeakSalesHourMetric> findPeakSalesHourByDay(LocalDate from, LocalDate to);

    List<OrderWeekMetric> findOrdersByWeekOfMonth(LocalDate from, LocalDate to);

    List<HourlySalesMetric> findSalesByHour(LocalDate date);

    List<TopDishMetric> findTopDishes(LocalDate from, LocalDate to, int limit);
}