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
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.modules.sales.domain.model.SaleSource;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompleteOrderUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private SaleRepositoryPort saleRepositoryPort;

    @Mock
    private OrderAuthorizationService authorizationService;

    @Mock
    private OrderResponseAssembler responseAssembler;

    @InjectMocks
    private CompleteOrderUseCase useCase;

    @Test
    void completesAcceptedOrderAndCreatesRemoteSale() {
        Order accepted = TestOrderData.acceptedOrder();
        Order completed = accepted.complete();
        OrderResponse expected = OrderResponse.from(completed, "Cliente Prueba", List.of(TestOrderData.orderItem()));
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(accepted));
        when(saleRepositoryPort.findByOrderIdForUpdate(10)).thenReturn(Optional.empty());
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(completed);
        when(saleRepositoryPort.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(completed, List.of(TestOrderData.orderItem()))).thenReturn(expected);

        assertThat(useCase.complete(10, TestOrderData.authenticatedEncargada())).isSameAs(expected);

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(orderRepositoryPort).save(savedOrder.capture());
        assertThat(savedOrder.getValue().estado()).isEqualTo(OrderStatus.FINALIZADO);
        assertThat(savedOrder.getValue().respondidoPor()).isEqualTo(accepted.respondidoPor());
        assertThat(savedOrder.getValue().respondidoEn()).isEqualTo(accepted.respondidoEn());

        ArgumentCaptor<Sale> savedSale = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepositoryPort).save(savedSale.capture());
        assertThat(savedSale.getValue().orderId()).isEqualTo(10);
        assertThat(savedSale.getValue().source()).isEqualTo(SaleSource.REMOTA);
        assertThat(savedSale.getValue().total()).isEqualByComparingTo("130.00");
        assertThat(savedSale.getValue().registradoPor()).isEqualTo(2);
    }

    @Test
    void completesAcceptedOrderWithExistingLegacySaleWithoutDuplicating() {
        Order accepted = TestOrderData.acceptedOrder();
        Order completed = accepted.complete();
        Sale existingSale = Sale.createRemote(10, accepted.total(), 2);
        OrderResponse expected = OrderResponse.from(completed, "Cliente Prueba", List.of(TestOrderData.orderItem()));
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(accepted));
        when(saleRepositoryPort.findByOrderIdForUpdate(10)).thenReturn(Optional.of(existingSale));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(completed);
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(completed, List.of(TestOrderData.orderItem()))).thenReturn(expected);

        assertThat(useCase.complete(10, TestOrderData.authenticatedEncargada())).isSameAs(expected);

        verify(orderRepositoryPort).save(any(Order.class));
        verify(saleRepositoryPort, never()).save(any(Sale.class));
    }

    @Test
    void repeatedCompletionWithExistingSaleIsIdempotent() {
        Order completed = TestOrderData.acceptedOrder().complete();
        Sale existingSale = Sale.createRemote(10, completed.total(), 2);
        OrderResponse expected = OrderResponse.from(completed, "Cliente Prueba", List.of(TestOrderData.orderItem()));
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(completed));
        when(saleRepositoryPort.findByOrderIdForUpdate(10)).thenReturn(Optional.of(existingSale));
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(completed, List.of(TestOrderData.orderItem()))).thenReturn(expected);

        assertThat(useCase.complete(10, TestOrderData.authenticatedEncargada())).isSameAs(expected);

        verify(orderRepositoryPort, never()).save(any());
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    void repeatedCompletionWithoutSaleIsConflict() {
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10))
                .thenReturn(Optional.of(TestOrderData.acceptedOrder().complete()));
        when(saleRepositoryPort.findByOrderIdForUpdate(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.complete(10, TestOrderData.authenticatedEncargada()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("El pedido finalizado no tiene una venta remota asociada.");

        verify(orderRepositoryPort, never()).save(any());
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsPendingOrder() {
        assertInvalidTransition(TestOrderData.pendingOrder());
    }

    @Test
    void rejectsRejectedOrder() {
        assertInvalidTransition(TestOrderData.pendingOrder().reject(2, "Sin existencias"));
    }

    @Test
    void rejectsCancelledOrder() {
        assertInvalidTransition(TestOrderData.acceptedOrder().cancel(2));
    }

    @Test
    void returnsNotFoundForMissingOrder() {
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.complete(999, TestOrderData.authenticatedEncargada()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No se encontro el pedido.");

        verify(orderRepositoryPort, never()).save(any());
        verify(saleRepositoryPort, never()).save(any());
    }

    private void assertInvalidTransition(Order order) {
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.complete(10, TestOrderData.authenticatedEncargada()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Solo los pedidos aceptados pueden finalizarse.");

        verify(orderRepositoryPort, never()).save(any());
        verify(saleRepositoryPort, never()).save(any());
    }
}