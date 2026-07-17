package com.puravida.modules.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.shared.domain.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetOrderDetailUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private OrderAuthorizationService authorizationService;

    @Mock
    private OrderResponseAssembler responseAssembler;

    @InjectMocks
    private GetOrderDetailUseCase useCase;

    @Test
    void returnsOwnedOrderWithItsItems() {
        OrderResponse response = OrderResponse.from(
                TestOrderData.pendingOrder(),
                "Cliente Prueba",
                List.of(TestOrderData.orderItem())
        );
        when(authorizationService.requireClienteForHistory(TestOrderData.authenticatedClient()))
                .thenReturn(TestOrderData.client());
        when(orderRepositoryPort.findByIdAndClientId(10, 1))
                .thenReturn(Optional.of(TestOrderData.pendingOrder()));
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(List.of(TestOrderData.orderItem()));
        when(responseAssembler.detail(TestOrderData.pendingOrder(), List.of(TestOrderData.orderItem())))
                .thenReturn(response);

        OrderResponse result = useCase.getOrder(10, TestOrderData.authenticatedClient());

        assertThat(result).isSameAs(response);
        verify(orderRepositoryPort).findItemsByOrderId(10);
    }

    @Test
    void hidesAnotherClientsOrderAsNotFound() {
        when(authorizationService.requireClienteForHistory(TestOrderData.authenticatedClient()))
                .thenReturn(TestOrderData.client());
        when(orderRepositoryPort.findByIdAndClientId(10, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getOrder(10, TestOrderData.authenticatedClient()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No se encontro el pedido.");

        verify(orderRepositoryPort, never()).findById(10);
        verify(orderRepositoryPort, never()).findItemsByOrderId(10);
    }

    @Test
    void returnsNotFoundForMissingOrder() {
        when(authorizationService.requireClienteForHistory(TestOrderData.authenticatedClient()))
                .thenReturn(TestOrderData.client());
        when(orderRepositoryPort.findByIdAndClientId(99, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getOrder(99, TestOrderData.authenticatedClient()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No se encontro el pedido.");

        verify(orderRepositoryPort, never()).findItemsByOrderId(99);
    }
}
