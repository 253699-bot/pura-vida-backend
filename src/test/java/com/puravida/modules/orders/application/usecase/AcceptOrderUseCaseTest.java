package com.puravida.modules.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.shared.domain.exception.ConflictException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AcceptOrderUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private OrderAuthorizationService authorizationService;

    @Mock
    private OrderResponseAssembler responseAssembler;

    @InjectMocks
    private AcceptOrderUseCase useCase;

    @Test
    void acceptsPendingOrderAndRegistersResponder() {
        Order accepted = TestOrderData.acceptedOrder();
        OrderResponse expected = OrderResponse.from(accepted, "Cliente Prueba", List.of(TestOrderData.orderItem()));
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada())).thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findById(10)).thenReturn(Optional.of(TestOrderData.pendingOrder()));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(accepted);
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(accepted, List.of(TestOrderData.orderItem()))).thenReturn(expected);

        assertThat(useCase.accept(10, TestOrderData.authenticatedEncargada())).isSameAs(expected);

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(orderRepositoryPort).save(savedOrder.capture());
        assertThat(savedOrder.getValue().estado().databaseValue()).isEqualTo("aceptado");
        assertThat(savedOrder.getValue().respondidoPor()).isEqualTo(2);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada())).thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findById(10)).thenReturn(Optional.of(TestOrderData.acceptedOrder()));

        assertThatThrownBy(() -> useCase.accept(10, TestOrderData.authenticatedEncargada()))
                .isInstanceOf(ConflictException.class);

        verify(orderRepositoryPort, never()).save(any());
    }
}
