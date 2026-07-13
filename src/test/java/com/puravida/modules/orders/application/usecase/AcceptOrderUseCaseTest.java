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
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.model.Sale;
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

    @InjectMocks
    private AcceptOrderUseCase useCase;

    @Test
    void acceptsPendingOrderAndRegistersResponder() {
        Order accepted = TestOrderData.acceptedOrder();
        OrderResponse expected = OrderResponse.from(accepted, "Cliente Prueba", List.of(TestOrderData.orderItem()));
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada())).thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(TestOrderData.pendingOrder()));
        when(saleRepositoryPort.existsByOrderId(10)).thenReturn(false);
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(accepted);
        when(saleRepositoryPort.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(accepted, List.of(TestOrderData.orderItem()))).thenReturn(expected);

        assertThat(useCase.accept(10, TestOrderData.authenticatedEncargada())).isSameAs(expected);

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(orderRepositoryPort).save(savedOrder.capture());
        assertThat(savedOrder.getValue().estado().databaseValue()).isEqualTo("aceptado");
        assertThat(savedOrder.getValue().respondidoPor()).isEqualTo(2);
        ArgumentCaptor<Sale> savedSale = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepositoryPort).save(savedSale.capture());
        assertThat(savedSale.getValue().orderId()).isEqualTo(10);
        assertThat(savedSale.getValue().source().databaseValue()).isEqualTo("remota");
        assertThat(savedSale.getValue().total()).isEqualByComparingTo("130.00");
    }

    @Test
    void returnsAcceptedOrderWithoutDuplicatingSaleOnRetry() {
        Order accepted = TestOrderData.acceptedOrder();
        Sale existingSale = Sale.createRemote(10, accepted.total(), 2);
        OrderResponse expected = OrderResponse.from(accepted, "Cliente Prueba", List.of(TestOrderData.orderItem()));
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada())).thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(accepted));
        when(saleRepositoryPort.findByOrderId(10)).thenReturn(Optional.of(existingSale));
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(accepted, List.of(TestOrderData.orderItem()))).thenReturn(expected);

        assertThat(useCase.accept(10, TestOrderData.authenticatedEncargada())).isSameAs(expected);

        verify(orderRepositoryPort, never()).save(any());
        verify(saleRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsAcceptedOrderWithoutAssociatedSale() {
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada())).thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findByIdForUpdate(10)).thenReturn(Optional.of(TestOrderData.acceptedOrder()));
        when(saleRepositoryPort.findByOrderId(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.accept(10, TestOrderData.authenticatedEncargada()))
                .isInstanceOf(ConflictException.class);

        verify(orderRepositoryPort, never()).save(any());
    }
}
