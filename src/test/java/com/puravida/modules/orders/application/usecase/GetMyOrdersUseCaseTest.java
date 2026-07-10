package com.puravida.modules.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.orders.application.dto.OrderSummaryResponse;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetMyOrdersUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private OrderAuthorizationService authorizationService;

    @Mock
    private OrderResponseAssembler responseAssembler;

    @InjectMocks
    private GetMyOrdersUseCase useCase;

    @Test
    void returnsOnlyOrdersForAuthenticatedUser() {
        OrderSummaryResponse summary = OrderSummaryResponse.from(TestOrderData.pendingOrder(), "Cliente Prueba");
        when(authorizationService.requireActiveUser(TestOrderData.authenticatedClient())).thenReturn(TestOrderData.client());
        when(orderRepositoryPort.findByClientId(1)).thenReturn(List.of(TestOrderData.pendingOrder()));
        when(responseAssembler.summary(TestOrderData.pendingOrder())).thenReturn(summary);

        List<OrderSummaryResponse> response = useCase.getMyOrders(TestOrderData.authenticatedClient());

        verify(orderRepositoryPort).findByClientId(1);
        assertThat(response).containsExactly(summary);
    }
}
