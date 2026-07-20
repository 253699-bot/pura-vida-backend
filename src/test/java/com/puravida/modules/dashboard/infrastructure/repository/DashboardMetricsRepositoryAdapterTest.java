package com.puravida.modules.dashboard.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.business.infrastructure.persistence.BusinessDayStatusJpaRepository;
import com.puravida.modules.dashboard.domain.model.DailySalesMetric;
import com.puravida.modules.dashboard.domain.model.HourlySalesMetric;
import com.puravida.modules.dashboard.domain.model.OrderMetrics;
import com.puravida.modules.dashboard.domain.model.SalesMetrics;
import com.puravida.modules.dashboard.domain.model.TopDishMetric;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.sales.domain.model.SaleSource;
import com.puravida.modules.sales.domain.model.SaleStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DashboardMetricsRepositoryAdapterTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Object[]> query;

    @Mock
    private Query nativeQuery;

    @Mock
    private BusinessDayStatusJpaRepository businessDayStatusRepository;

    private DashboardMetricsRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DashboardMetricsRepositoryAdapter(businessDayStatusRepository);
        ReflectionTestUtils.setField(adapter, "entityManager", entityManager);
    }

    @Test
    void acceptedMetricIncludesCompletedOrdersAndCancelledMetric() {
        when(entityManager.createQuery(anyString(), org.mockito.ArgumentMatchers.eq(Object[].class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Object[]{2L, 5L, 1L, 4L});

        OrderMetrics metrics = adapter.aggregateOrders(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );

        assertThat(metrics.accepted()).isEqualTo(5);
        assertThat(metrics.cancelled()).isEqualTo(4);
        verify(query).setParameter("finalized", OrderStatus.FINALIZADO);
        verify(query).setParameter("cancelled", OrderStatus.CANCELADO);
        ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(jpql.capture(), org.mockito.ArgumentMatchers.eq(Object[].class));
        assertThat(jpql.getValue())
                .contains("entity.estado = :accepted OR entity.estado = :finalized")
                .contains("entity.estado = :cancelled");
    }

    @Test
    void aggregateSalesKeepsActiveManualSalesAndRestrictsRemoteSalesToFinalizedOrders() {
        when(entityManager.createQuery(anyString(), org.mockito.ArgumentMatchers.eq(Object[].class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Object[]{
                new BigDecimal("480.00"),
                new BigDecimal("50.00"),
                4L,
                1L,
                new BigDecimal("180.00"),
                2L,
                new BigDecimal("300.00"),
                2L
        });

        SalesMetrics metrics = adapter.aggregateSales(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );

        assertThat(metrics.activeTotal()).isEqualByComparingTo("480.00");
        assertThat(metrics.activeCount()).isEqualTo(4);
        assertThat(metrics.manual().count()).isEqualTo(2);
        assertThat(metrics.manual().total()).isEqualByComparingTo("180.00");
        assertThat(metrics.remote().count()).isEqualTo(2);
        assertThat(metrics.remote().total()).isEqualByComparingTo("300.00");
        verify(query).setParameter("manual", SaleSource.MANUAL_FONDA);
        verify(query).setParameter("remote", SaleSource.REMOTA);
        verify(query).setParameter("finalized", OrderStatus.FINALIZADO);
        verify(query).setParameter("active", SaleStatus.ACTIVA);

        ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(jpql.capture(), org.mockito.ArgumentMatchers.eq(Object[].class));
        assertThat(jpql.getValue())
                .contains("sale.source = :manual OR (sale.source = :remote")
                .contains("orderEntity.estado = :finalized")
                .contains("LEFT JOIN OrderEntity orderEntity ON orderEntity.id = sale.orderId");
    }

    @Test
    void salesByDayFillsDatesWithoutSalesAndUsesValidActiveSalesOnly() {
        LocalDate from = LocalDate.of(2026, 7, 6);
        LocalDate to = LocalDate.of(2026, 7, 8);
        when(entityManager.createQuery(anyString(), org.mockito.ArgumentMatchers.eq(Object[].class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.<Object[]>of(
                new Object[]{LocalDate.of(2026, 7, 7), new BigDecimal("250.00"), 3L}
        ));

        List<DailySalesMetric> result = adapter.findSalesByDay(from, to);

        assertThat(result).containsExactly(
                new DailySalesMetric(LocalDate.of(2026, 7, 6), BigDecimal.ZERO, 0),
                new DailySalesMetric(LocalDate.of(2026, 7, 7), new BigDecimal("250.00"), 3),
                new DailySalesMetric(LocalDate.of(2026, 7, 8), BigDecimal.ZERO, 0)
        );
        ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(jpql.capture(), org.mockito.ArgumentMatchers.eq(Object[].class));
        assertThat(jpql.getValue())
                .contains("sale.status = :active")
                .contains("sale.source = :manual OR (sale.source = :remote")
                .contains("orderEntity.estado = :finalized");
    }

    @Test
    void salesByHourFillsAllHoursAndUsesValidActiveSalesOnly() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.<Object[]>of(
                new Object[]{13, new BigDecimal("180.00"), 2L}
        ));

        List<HourlySalesMetric> result = adapter.findSalesByHour(LocalDate.of(2026, 7, 11));

        assertThat(result).hasSize(24);
        assertThat(result.get(0)).isEqualTo(new HourlySalesMetric(0, BigDecimal.ZERO, 0));
        assertThat(result.get(13)).isEqualTo(new HourlySalesMetric(13, new BigDecimal("180.00"), 2));
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sql.capture());
        assertThat(sql.getValue())
                .contains("sale.Estado = 'activa'")
                .contains("sale.Fuente = 'manual_fonda'")
                .contains("customer_order.Estado = 'finalizado'");
    }

    @Test
    void topDishesUsesConsolidatedManualAndRemoteDetailsAndMapsNativeNumbers() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.setMaxResults(10)).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.<Object[]>of(
                new Object[]{5L, "Tacos", 7L, new BigDecimal("455.00")},
                new Object[]{8L, "Agua fresca", 3L, new BigDecimal("90.00")}
        ));

        List<TopDishMetric> result = adapter.findTopDishes(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                10
        );

        assertThat(result).containsExactly(
                new TopDishMetric(5, "Tacos", 7, new BigDecimal("455.00")),
                new TopDishMetric(8, "Agua fresca", 3, new BigDecimal("90.00"))
        );
        verify(nativeQuery).setMaxResults(10);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sql.capture());
        assertThat(sql.getValue())
                .contains("detail.Id_venta = sale.Id_venta")
                .contains("detail.Id_pedido = sale.Id_pedido")
                .contains("sale.Fuente = 'manual_fonda'")
                .contains("sale.Fuente = 'remota'")
                .contains("customer_order.Estado = 'finalizado'")
                .contains("sale.Estado = 'activa'");
    }
}
