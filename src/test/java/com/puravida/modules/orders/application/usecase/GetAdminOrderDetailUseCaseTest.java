package com.puravida.modules.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAdminOrderDetailUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private OrderAuthorizationService authorizationService;

    @Mock
    private OrderResponseAssembler responseAssembler;

    @InjectMocks
    private GetAdminOrderDetailUseCase useCase;

    @Test
    void returnsAnyOrderDetailForEncargada() {
        var order = TestOrderData.acceptedOrder();
        var items = List.of(TestOrderData.orderItem());
        var expected = OrderResponse.from(order, "Cliente Prueba", items);
        when(authorizationService.requireEncargada(TestOrderData.authenticatedEncargada()))
                .thenReturn(TestOrderData.encargada());
        when(orderRepositoryPort.findById(10)).thenReturn(Optional.of(order));
        when(orderRepositoryPort.findItemsByOrderId(10)).thenReturn(items);
        when(responseAssembler.detail(order, items)).thenReturn(expected);

        assertThat(useCase.getOrder(10, TestOrderData.authenticatedEncargada())).isSameAs(expected);
    }
}
