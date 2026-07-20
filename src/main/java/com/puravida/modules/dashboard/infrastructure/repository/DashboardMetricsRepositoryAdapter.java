package com.puravida.modules.dashboard.infrastructure.repository;

import com.puravida.modules.business.infrastructure.persistence.BusinessDayStatusJpaRepository;
import com.puravida.modules.dashboard.application.port.out.DashboardMetricsRepositoryPort;
import com.puravida.modules.dashboard.domain.model.DailySalesMetric;
import com.puravida.modules.dashboard.domain.model.HourlySalesMetric;
import com.puravida.modules.dashboard.domain.model.OperationMetrics;
import com.puravida.modules.dashboard.domain.model.OrderMetrics;
import com.puravida.modules.dashboard.domain.model.OrderStatusMetrics;
import com.puravida.modules.dashboard.domain.model.OrderWeekMetric;
import com.puravida.modules.dashboard.domain.model.PeakSalesHourMetric;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                            COALESCE(SUM(CASE WHEN sale.status = :active
                                AND (sale.source = :manual OR (sale.source = :remote
                                    AND orderEntity.estado = :finalized))
                                THEN sale.total ELSE 0 END), 0),
                            COALESCE(SUM(CASE WHEN sale.status = :cancelled THEN sale.total ELSE 0 END), 0),
                            SUM(CASE WHEN sale.status = :active
                                AND (sale.source = :manual OR (sale.source = :remote
                                    AND orderEntity.estado = :finalized))
                                THEN 1 ELSE 0 END),
                            SUM(CASE WHEN sale.status = :cancelled THEN 1 ELSE 0 END),
                            COALESCE(SUM(CASE WHEN sale.status = :active AND sale.source = :manual
                                THEN sale.total ELSE 0 END), 0),
                            SUM(CASE WHEN sale.status = :active AND sale.source = :manual THEN 1 ELSE 0 END),
                            COALESCE(SUM(CASE WHEN sale.status = :active AND sale.source = :remote
                                AND orderEntity.estado = :finalized
                                THEN sale.total ELSE 0 END), 0),
                            SUM(CASE WHEN sale.status = :active AND sale.source = :remote
                                AND orderEntity.estado = :finalized THEN 1 ELSE 0 END)
                        FROM SaleEntity sale
                        LEFT JOIN OrderEntity orderEntity ON orderEntity.id = sale.orderId
                        WHERE sale.fecha BETWEEN :from AND :to
                        """, Object[].class)
                .setParameter("active", SaleStatus.ACTIVA)
                .setParameter("cancelled", SaleStatus.ANULADA)
                .setParameter("manual", SaleSource.MANUAL_FONDA)
                .setParameter("remote", SaleSource.REMOTA)
                .setParameter("finalized", OrderStatus.FINALIZADO)
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
                            SUM(CASE WHEN entity.estado = :accepted OR entity.estado = :finalized
                                THEN 1 ELSE 0 END),
                            SUM(CASE WHEN entity.estado = :rejected THEN 1 ELSE 0 END),
                            SUM(CASE WHEN entity.estado = :cancelled THEN 1 ELSE 0 END)
                        FROM OrderEntity entity
                        WHERE entity.fecha BETWEEN :from AND :to
                        """, Object[].class)
                .setParameter("pending", OrderStatus.PENDIENTE)
                .setParameter("accepted", OrderStatus.ACEPTADO)
                .setParameter("finalized", OrderStatus.FINALIZADO)
                .setParameter("rejected", OrderStatus.RECHAZADO)
                .setParameter("cancelled", OrderStatus.CANCELADO)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        return new OrderMetrics(number(row[0]), number(row[1]), number(row[2]), number(row[3]));
    }

    @Override
    public OrderStatusMetrics aggregateOrderStatuses(LocalDate from, LocalDate to) {
        Object[] row = entityManager.createQuery("""
                        SELECT
                            SUM(CASE WHEN entity.estado = :pending THEN 1 ELSE 0 END),
                            SUM(CASE WHEN entity.estado = :accepted THEN 1 ELSE 0 END),
                            SUM(CASE WHEN entity.estado = :rejected THEN 1 ELSE 0 END),
                            SUM(CASE WHEN entity.estado = :finalized THEN 1 ELSE 0 END)
                        FROM OrderEntity entity
                        WHERE entity.fecha BETWEEN :from AND :to
                        """, Object[].class)
                .setParameter("pending", OrderStatus.PENDIENTE)
                .setParameter("accepted", OrderStatus.ACEPTADO)
                .setParameter("rejected", OrderStatus.RECHAZADO)
                .setParameter("finalized", OrderStatus.FINALIZADO)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        return new OrderStatusMetrics(number(row[0]), number(row[1]), number(row[2]), number(row[3]));
    }

    @Override
    public OperationMetrics findOperation(LocalDate date) {
        return businessDayStatusRepository.findByFecha(date)
                .map(entity -> entity.toDomain())
                .map(status -> new OperationMetrics(true, status.abierto(), status.motivoCierre()))
                .orElseGet(OperationMetrics::notConfigured);
    }

    @Override
    public List<DailySalesMetric> findSalesByDay(LocalDate from, LocalDate to) {
        List<Object[]> rows = entityManager.createQuery("""
                        SELECT sale.fecha, COALESCE(SUM(sale.total), 0), COUNT(sale.id)
                        FROM SaleEntity sale
                        LEFT JOIN OrderEntity orderEntity ON orderEntity.id = sale.orderId
                        WHERE sale.fecha BETWEEN :from AND :to
                          AND sale.status = :active
                          AND (sale.source = :manual OR (sale.source = :remote
                            AND orderEntity.estado = :finalized))
                        GROUP BY sale.fecha
                        """, Object[].class)
                .setParameter("active", SaleStatus.ACTIVA)
                .setParameter("manual", SaleSource.MANUAL_FONDA)
                .setParameter("remote", SaleSource.REMOTA)
                .setParameter("finalized", OrderStatus.FINALIZADO)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        Map<LocalDate, DailySalesMetric> metricsByDate = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate date = (LocalDate) row[0];
            metricsByDate.put(date, new DailySalesMetric(date, decimal(row[1]), number(row[2])));
        }

        return from.datesUntil(to.plusDays(1))
                .map(date -> metricsByDate.getOrDefault(date, new DailySalesMetric(date, BigDecimal.ZERO, 0)))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PeakSalesHourMetric> findPeakSalesHourByDay(LocalDate from, LocalDate to) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                        SELECT
                            sale.Fecha,
                            HOUR(sale.Hora),
                            COUNT(sale.Id_venta)
                        FROM VENTAS sale
                        LEFT JOIN PEDIDOS customer_order
                          ON customer_order.Id_pedido = sale.Id_pedido
                        WHERE sale.Fecha BETWEEN :from AND :to
                          AND sale.Estado = 'activa'
                          AND (
                            sale.Fuente = 'manual_fonda'
                            OR (sale.Fuente = 'remota' AND customer_order.Estado = 'finalizado')
                          )
                        GROUP BY sale.Fecha, HOUR(sale.Hora)
                        ORDER BY sale.Fecha ASC, COUNT(sale.Id_venta) DESC, HOUR(sale.Hora) ASC
                        """)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        Map<LocalDate, PeakSalesHourMetric> peakByDate = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate date = date(row[0]);
            peakByDate.putIfAbsent(
                    date,
                    new PeakSalesHourMetric(date, ((Number) row[1]).intValue(), number(row[2]))
            );
        }

        return from.datesUntil(to.plusDays(1))
                .map(date -> peakByDate.getOrDefault(date, new PeakSalesHourMetric(date, null, 0)))
                .toList();
    }

    @Override
    public List<OrderWeekMetric> findOrdersByWeekOfMonth(LocalDate from, LocalDate to) {
        List<Object[]> rows = entityManager.createQuery("""
                        SELECT entity.fecha, COUNT(entity.id)
                        FROM OrderEntity entity
                        WHERE entity.fecha BETWEEN :from AND :to
                          AND entity.estado IN :validStates
                        GROUP BY entity.fecha
                        """, Object[].class)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("validStates", List.of(OrderStatus.ACEPTADO, OrderStatus.FINALIZADO))
                .getResultList();

        Map<Integer, Long> countsByWeek = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate date = (LocalDate) row[0];
            int week = weekOfMonth(date);
            countsByWeek.merge(week, number(row[1]), Long::sum);
        }

        int firstWeek = weekOfMonth(from);
        int lastWeek = weekOfMonth(to);
        return java.util.stream.IntStream.rangeClosed(firstWeek, lastWeek)
                .mapToObj(week -> new OrderWeekMetric(
                        week,
                        firstDayForWeek(from, to, week),
                        lastDayForWeek(from, to, week),
                        countsByWeek.getOrDefault(week, 0L)
                ))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HourlySalesMetric> findSalesByHour(LocalDate date) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                        SELECT
                            HOUR(sale.Hora),
                            COALESCE(SUM(sale.Total), 0),
                            COUNT(sale.Id_venta)
                        FROM VENTAS sale
                        LEFT JOIN PEDIDOS customer_order
                          ON customer_order.Id_pedido = sale.Id_pedido
                        WHERE sale.Fecha = :date
                          AND sale.Estado = 'activa'
                          AND (
                            sale.Fuente = 'manual_fonda'
                            OR (sale.Fuente = 'remota' AND customer_order.Estado = 'finalizado')
                          )
                        GROUP BY HOUR(sale.Hora)
                        """)
                .setParameter("date", date)
                .getResultList();

        Map<Integer, HourlySalesMetric> metricsByHour = new HashMap<>();
        for (Object[] row : rows) {
            int hour = ((Number) row[0]).intValue();
            metricsByHour.put(hour, new HourlySalesMetric(hour, decimal(row[1]), number(row[2])));
        }

        return java.util.stream.IntStream.range(0, 24)
                .mapToObj(hour -> metricsByHour.getOrDefault(hour, new HourlySalesMetric(hour, BigDecimal.ZERO, 0)))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TopDishMetric> findTopDishes(LocalDate from, LocalDate to, int limit) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                        SELECT
                            detail.Id_platillo,
                            detail.Nombre_platillo,
                            SUM(detail.Cantidad),
                            SUM(detail.Subtotal)
                        FROM DETALLE_PEDIDO detail
                        INNER JOIN VENTAS sale
                          ON (sale.Fuente = 'manual_fonda' AND detail.Id_venta = sale.Id_venta)
                          OR (sale.Fuente = 'remota' AND detail.Id_pedido = sale.Id_pedido)
                        LEFT JOIN PEDIDOS customer_order
                          ON customer_order.Id_pedido = sale.Id_pedido
                        WHERE sale.Estado = 'activa'
                          AND sale.Fecha BETWEEN :from AND :to
                          AND (
                            sale.Fuente = 'manual_fonda'
                            OR customer_order.Estado = 'finalizado'
                          )
                        GROUP BY detail.Id_platillo, detail.Nombre_platillo
                        ORDER BY SUM(detail.Cantidad) DESC, SUM(detail.Subtotal) DESC
                        """)
                .setParameter("from", from)
                .setParameter("to", to)
                .setMaxResults(limit)
                .getResultList();
        return rows.stream()
                .map(row -> new TopDishMetric(
                        row[0] == null ? null : ((Number) row[0]).intValue(),
                        (String) row[1],
                        number(row[2]),
                        decimal(row[3])
                ))
                .toList();
    }

    private int weekOfMonth(LocalDate date) {
        return ((date.getDayOfMonth() - 1) / 7) + 1;
    }

    private LocalDate firstDayForWeek(LocalDate from, LocalDate to, int week) {
        LocalDate monthStart = from.withDayOfMonth(1).plusDays((long) (week - 1) * 7);
        if (monthStart.isBefore(from)) {
            return from;
        }
        return monthStart.isAfter(to) ? to : monthStart;
    }

    private LocalDate lastDayForWeek(LocalDate from, LocalDate to, int week) {
        LocalDate weekEnd = from.withDayOfMonth(1).plusDays((long) week * 7 - 1);
        return weekEnd.isAfter(to) ? to : weekEnd;
    }

    private LocalDate date(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private BigDecimal decimal(Object value) {
        return value == null ? BigDecimal.ZERO : (BigDecimal) value;
    }

    private long number(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }
}