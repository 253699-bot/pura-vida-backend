package com.puravida.modules.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.notifications.application.port.in.OrderNotificationPort;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.modules.sales.domain.model.SaleStatus;
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
class CancelOrderUseCaseTest {

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
    private CancelOrderUseCase useCase;

    @Test
    void cancelsAcceptedOrderWithoutCreatingSale() {
        Order accepted = TestOrderData.acceptedOrder();
        Order cancelled = accepted.cancel(2);
        OrderResponse expected = OrderResponse.from(
                cancelled,
                "Cliente Prueba",
                List.of(TestOrderData.orderItem())
        );
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(accepted));
        when(saleRepositoryPort.findByOrderIdForUpdate(10)).thenReturn(Optional.empty());
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(cancelled);
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(cancelled, List.of(TestOrderData.orderItem()))).thenReturn(expected);

        assertThat(useCase.cancel(10, TestOrderData.authenticatedEncargada())).isSameAs(expected);

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(orderRepositoryPort).save(savedOrder.capture());
        assertThat(savedOrder.getValue().estado()).isEqualTo(OrderStatus.CANCELADO);
        assertThat(savedOrder.getValue().canceladoPor()).isEqualTo(2);
        assertThat(savedOrder.getValue().canceladoEn()).isNotNull();
        verify(saleRepositoryPort, never()).save(any(Sale.class));
        verify(orderNotificationPort).notifyOrderCancelled(10, 1);
    }

    @Test
    void cancelsAcceptedOrderAndLegacyRemoteSaleAtomically() {
        Order accepted = TestOrderData.acceptedOrder();
        Sale activeSale = Sale.createRemote(10, accepted.total(), 2);
        OrderResponse expected = OrderResponse.from(
                accepted.cancel(2),
                "Cliente Prueba",
                List.of(TestOrderData.orderItem())
        );
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(accepted));
        when(saleRepositoryPort.findByOrderIdForUpdate(10)).thenReturn(Optional.of(activeSale));
        when(saleRepositoryPort.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(any(Order.class), any())).thenReturn(expected);

        assertThat(useCase.cancel(10, TestOrderData.authenticatedEncargada())).isSameAs(expected);

        ArgumentCaptor<Sale> savedSale = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepositoryPort).save(savedSale.capture());
        assertThat(savedSale.getValue().status()).isEqualTo(SaleStatus.ANULADA);
        assertThat(savedSale.getValue().motivoAnulacion())
                .isEqualTo("Pedido cancelado por la encargada.");
        assertThat(savedSale.getValue().usuarioAnuloId()).isEqualTo(2);
        verify(orderNotificationPort).notifyOrderCancelled(10, 1);
    }

    @Test
    void returnsCancelledOrderWithoutSaleWithoutWritingAgain() {
        Order cancelled = TestOrderData.acceptedOrder().cancel(2);
        OrderResponse expected = OrderResponse.from(
                cancelled,
                "Cliente Prueba",
                List.of(TestOrderData.orderItem())
        );
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(cancelled));
        when(saleRepositoryPort.findByOrderIdForUpdate(10)).thenReturn(Optional.empty());
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(cancelled, List.of(TestOrderData.orderItem()))).thenReturn(expected);

        assertThat(useCase.cancel(10, TestOrderData.authenticatedEncargada())).isSameAs(expected);

        verify(orderRepositoryPort, never()).save(any());
        verify(saleRepositoryPort, never()).save(any());
        verify(orderNotificationPort, never()).notifyOrderCancelled(any(), any());
    }

    @Test
    void returnsCoherentCancelledOrderWithLegacySaleWithoutWritingAgain() {
        Order cancelled = TestOrderData.acceptedOrder().cancel(2);
        Sale cancelledSale = Sale.createRemote(10, cancelled.total(), 2)
                .cancel("Pedido cancelado por la encargada.", 2);
        OrderResponse expected = OrderResponse.from(
                cancelled,
                "Cliente Prueba",
                List.of(TestOrderData.orderItem())
        );
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(cancelled));
        when(saleRepositoryPort.findByOrderIdForUpdate(10)).thenReturn(Optional.of(cancelledSale));
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(cancelled, List.of(TestOrderData.orderItem()))).thenReturn(expected);

        assertThat(useCase.cancel(10, TestOrderData.authenticatedEncargada())).isSameAs(expected);

        verify(orderRepositoryPort, never()).save(any());
        verify(saleRepositoryPort, never()).save(any());
        verify(orderNotificationPort, never()).notifyOrderCancelled(any(), any());
    }

    @Test
    void rejectsNonAcceptedOrderBeforeLookingForSale() {
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10))
                .thenReturn(Optional.of(TestOrderData.pendingOrder()));

        assertThatThrownBy(() -> useCase.cancel(10, TestOrderData.authenticatedEncargada()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Solo los pedidos aceptados pueden cancelarse.");

        verify(saleRepositoryPort, never()).findByOrderIdForUpdate(any());
        verify(orderNotificationPort, never()).notifyOrderCancelled(any(), any());
    }
}