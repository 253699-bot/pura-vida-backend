package com.puravida.modules.dashboard.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.business.infrastructure.persistence.BusinessDayStatusJpaRepository;
import com.puravida.modules.dashboard.domain.model.OrderMetrics;
import com.puravida.modules.orders.domain.model.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
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
    private BusinessDayStatusJpaRepository businessDayStatusRepository;

    private DashboardMetricsRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DashboardMetricsRepositoryAdapter(businessDayStatusRepository);
        ReflectionTestUtils.setField(adapter, "entityManager", entityManager);
    }

    @Test
    void acceptedMetricIncludesCompletedOrders() {
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Object[]{2L, 5L, 1L});

        OrderMetrics metrics = adapter.aggregateOrders(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );

        assertThat(metrics.accepted()).isEqualTo(5);
        verify(query).setParameter("finalized", OrderStatus.FINALIZADO);
        ArgumentCaptor<String> jpql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createQuery(jpql.capture(), eq(Object[].class));
        assertThat(jpql.getValue()).contains("entity.estado = :accepted OR entity.estado = :finalized");
    }
}
