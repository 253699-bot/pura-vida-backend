package com.puravida.modules.dashboard.application.port.out;

import com.puravida.modules.dashboard.domain.model.OperationMetrics;
import com.puravida.modules.dashboard.domain.model.OrderMetrics;
import com.puravida.modules.dashboard.domain.model.SalesMetrics;
import com.puravida.modules.dashboard.domain.model.TopDishMetric;
import java.time.LocalDate;
import java.util.List;

public interface DashboardMetricsRepositoryPort {

    SalesMetrics aggregateSales(LocalDate from, LocalDate to);

    OrderMetrics aggregateOrders(LocalDate from, LocalDate to);

    OperationMetrics findOperation(LocalDate date);

    List<TopDishMetric> findTopDishes(LocalDate from, LocalDate to, int limit);
}
