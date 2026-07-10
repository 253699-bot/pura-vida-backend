package com.puravida.modules.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.dto.RejectOrderRequest;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.exception.OrderValidationException;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RejectOrderUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private OrderAuthorizationService authorizationService;

    @Mock
    private OrderResponseAssembler responseAssembler;

    @InjectMocks
    private RejectOrderUseCase useCase;

    @Test
    void rejectsPendingOrderWithReason() {
        Order rejected = TestOrderData.pendingOrder().reject(2, "No hay tortillas disponibles");
        OrderResponse expected = OrderResponse.from(rejected, "Cliente Prueba", List.of(TestOrderData.orderItem()));
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada())).thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findById(10)).thenReturn(Optional.of(TestOrderData.pendingOrder()));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(rejected);
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(rejected, List.of(TestOrderData.orderItem()))).thenReturn(expected);

        assertThat(useCase.reject(
                10,
                new RejectOrderRequest(" No hay tortillas disponibles "),
                TestOrderData.authenticatedEncargada()
        )).isSameAs(expected);

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(orderRepositoryPort).save(savedOrder.capture());
        assertThat(savedOrder.getValue().estado()).isEqualTo(OrderStatus.RECHAZADO);
        assertThat(savedOrder.getValue().motivoRechazo()).isEqualTo("No hay tortillas disponibles");
    }

    @Test
    void requiresRejectReason() {
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada())).thenReturn(TestOrderData.encargada());

        assertThatThrownBy(() -> useCase.reject(
                10,
                new RejectOrderRequest(" "),
                TestOrderData.authenticatedEncargada()
        )).isInstanceOf(OrderValidationException.class);
    }
}
