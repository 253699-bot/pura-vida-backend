package com.puravida.modules.dashboard.infrastructure.repository;

import com.puravida.modules.business.infrastructure.persistence.BusinessDayStatusJpaRepository;
import com.puravida.modules.dashboard.application.port.out.DashboardMetricsRepositoryPort;
import com.puravida.modules.dashboard.domain.model.OperationMetrics;
import com.puravida.modules.dashboard.domain.model.OrderMetrics;
import com.puravida.modules.dashboard.domain.model.SalesMetrics;
import com.puravida.modules.dashboard.domain.model.SourceSalesMetrics;
import com.puravida.modules.dashboard.domain.model.TopDishMetric;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.sales.domain.model.SaleSource;
import com.puravida.modules.sales.domain.model.SaleStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardMetricsRepositoryAdapter implements DashboardMetricsRepositoryPort {

    @PersistenceContext
    private EntityManager entityManager;

    private final BusinessDayStatusJpaRepository businessDayStatusRepository;

    public DashboardMetricsRepositoryAdapter(BusinessDayStatusJpaRepository businessDayStatusRepository) {
        this.businessDayStatusRepository = businessDayStatusRepository;
    }

    @Override
    public SalesMetrics aggregateSales(LocalDate from, LocalDate to) {
        Object[] row = entityManager.createQuery("""
                        SELECT
                            COALESCE(SUM(CASE WHEN sale.status = :active THEN sale.total ELSE 0 END), 0),
                            COALESCE(SUM(CASE WHEN sale.status = :cancelled THEN sale.total ELSE 0 END), 0),
                            SUM(CASE WHEN sale.status = :active THEN 1 ELSE 0 END),
                            SUM(CASE WHEN sale.status = :cancelled THEN 1 ELSE 0 END),
                            COALESCE(SUM(CASE WHEN sale.status = :active AND sale.source = :manual
                                THEN sale.total ELSE 0 END), 0),
                            SUM(CASE WHEN sale.status = :active AND sale.source = :manual THEN 1 ELSE 0 END),
                            COALESCE(SUM(CASE WHEN sale.status = :active AND sale.source = :remote
                                THEN sale.total ELSE 0 END), 0),
                            SUM(CASE WHEN sale.status = :active AND sale.source = :remote THEN 1 ELSE 0 END)
                        FROM SaleEntity sale
                        WHERE sale.fecha BETWEEN :from AND :to
                        """, Object[].class)
                .setParameter("active", SaleStatus.ACTIVA)
                .setParameter("cancelled", SaleStatus.ANULADA)
                .setParameter("manual", SaleSource.MANUAL_FONDA)
                .setParameter("remote", SaleSource.REMOTA)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        return new SalesMetrics(
                decimal(row[0]),
                decimal(row[1]),
                number(row[2]),
                number(row[3]),
                new SourceSalesMetrics(number(row[5]), decimal(row[4])),
                new SourceSalesMetrics(number(row[7]), decimal(row[6]))
        );
    }

    @Override
    public OrderMetrics aggregateOrders(LocalDate from, LocalDate to) {
        Object[] row = entityManager.createQuery("""
                        SELECT
                            SUM(CASE WHEN entity.estado = :pending THEN 1 ELSE 0 END),
                            SUM(CASE WHEN entity.estado = :accepted THEN 1 ELSE 0 END),
                            SUM(CASE WHEN entity.estado = :rejected THEN 1 ELSE 0 END)
                        FROM OrderEntity entity
                        WHERE entity.fecha BETWEEN :from AND :to
                        """, Object[].class)
                .setParameter("pending", OrderStatus.PENDIENTE)
                .setParameter("accepted", OrderStatus.ACEPTADO)
                .setParameter("rejected", OrderStatus.RECHAZADO)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        return new OrderMetrics(number(row[0]), number(row[1]), number(row[2]));
    }

    @Override
    public OperationMetrics findOperation(LocalDate date) {
        return businessDayStatusRepository.findByFecha(date)
                .map(entity -> entity.toDomain())
                .map(status -> new OperationMetrics(true, status.abierto(), status.motivoCierre()))
                .orElseGet(OperationMetrics::notConfigured);
    }

    @Override
    public List<TopDishMetric> findTopDishes(LocalDate from, LocalDate to, int limit) {
        return entityManager.createQuery("""
                        SELECT item.dishId, item.nombrePlatillo, SUM(item.cantidad), SUM(item.subtotal)
                        FROM SaleEntity sale, OrderItemEntity item
                        WHERE sale.orderId = item.orderId
                          AND sale.status = :active
                          AND sale.source = :remote
                          AND sale.fecha BETWEEN :from AND :to
                        GROUP BY item.dishId, item.nombrePlatillo
                        ORDER BY SUM(item.cantidad) DESC, SUM(item.subtotal) DESC
                        """, Object[].class)
                .setParameter("active", SaleStatus.ACTIVA)
                .setParameter("remote", SaleSource.REMOTA)
                .setParameter("from", from)
                .setParameter("to", to)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(row -> new TopDishMetric(
                        (Integer) row[0],
                        (String) row[1],
                        number(row[2]),
                        decimal(row[3])
                ))
                .toList();
    }

    private BigDecimal decimal(Object value) {
        return value == null ? BigDecimal.ZERO : (BigDecimal) value;
    }

    private long number(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }
}
