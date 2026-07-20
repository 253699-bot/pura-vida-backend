package com.puravida.modules.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.puravida.modules.notifications.application.port.in.OrderNotificationPort;
import com.puravida.modules.orders.application.dto.AcceptOrderRequest;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
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
    private SaleRepositoryPort saleRepositoryPort;

    @Mock
    private OrderAuthorizationService authorizationService;

    @Mock
    private OrderResponseAssembler responseAssembler;

    @Mock
    private OrderNotificationPort orderNotificationPort;

    @InjectMocks
    private AcceptOrderUseCase useCase;

    @Test
    void acceptsPendingOrderAndDoesNotCreateRemoteSale() {
        Order accepted = TestOrderData.acceptedOrder();
        OrderResponse expected = OrderResponse.from(accepted, "Cliente Prueba", List.of(TestOrderData.orderItem()));
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(TestOrderData.pendingOrder()));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(accepted);
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(accepted, List.of(TestOrderData.orderItem()))).thenReturn(expected);

        assertThat(useCase.accept(
                10,
                new AcceptOrderRequest(" 25 minutos "),
                TestOrderData.authenticatedEncargada()
        )).isSameAs(expected);

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(orderRepositoryPort).save(savedOrder.capture());
        assertThat(savedOrder.getValue().estado().databaseValue()).isEqualTo("aceptado");
        assertThat(savedOrder.getValue().respondidoPor()).isEqualTo(2);
        assertThat(savedOrder.getValue().tiempoEsperaEstimado()).isEqualTo("25 minutos");
        verifyNoInteractions(saleRepositoryPort);
        verify(orderNotificationPort).notifyOrderAccepted(10, 1);
    }

    @Test
    void returnsAcceptedOrderOnRetryWithoutLookingForSale() {
        Order accepted = TestOrderData.acceptedOrder();
        OrderResponse expected = OrderResponse.from(accepted, "Cliente Prueba", List.of(TestOrderData.orderItem()));
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(accepted));
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(accepted, List.of(TestOrderData.orderItem()))).thenReturn(expected);

        assertThat(useCase.accept(
                10,
                new AcceptOrderRequest("25 minutos"),
                TestOrderData.authenticatedEncargada()
        )).isSameAs(expected);

        verify(orderRepositoryPort, never()).save(any());
        verifyNoInteractions(saleRepositoryPort);
        verify(orderNotificationPort, never()).notifyOrderAccepted(any(), any());
    }

    @Test
    void rejectsRetryWithDifferentEstimatedWait() {
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10))
                .thenReturn(Optional.of(TestOrderData.acceptedOrder()));

        assertThatThrownBy(() -> useCase.accept(
                10,
                new AcceptOrderRequest("40 minutos"),
                TestOrderData.authenticatedEncargada()
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("El pedido ya fue aceptado con un tiempo de espera diferente.");

        verify(orderRepositoryPort, never()).save(any());
        verifyNoInteractions(saleRepositoryPort);
        verify(orderNotificationPort, never()).notifyOrderAccepted(any(), any());
    }
}