package com.puravida.modules.orders.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.infrastructure.persistence.OrderEntity;
import com.puravida.modules.orders.infrastructure.persistence.OrderItemJpaRepository;
import com.puravida.modules.orders.infrastructure.persistence.OrderJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderRepositoryAdapterTest {

    @Mock
    private OrderJpaRepository orderJpaRepository;

    @Mock
    private OrderItemJpaRepository orderItemJpaRepository;

    private OrderRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OrderRepositoryAdapter(orderJpaRepository, orderItemJpaRepository);
    }

    @Test
    void readsOrderThroughPessimisticLockRepositoryMethod() {
        Order accepted = Order.create(
                1,
                LocalDate.of(2026, 7, 18),
                LocalTime.of(12, 0),
                new BigDecimal("130.00"),
                null
        ).accept(2, "25 minutos");
        when(orderJpaRepository.findByIdForUpdate(10))
                .thenReturn(Optional.of(OrderEntity.fromDomain(accepted)));

        assertThat(adapter.findByIdForUpdate(10))
                .get()
                .extracting(Order::tiempoEsperaEstimado)
                .isEqualTo("25 minutos");
        verify(orderJpaRepository).findByIdForUpdate(10);
    }
}
