package com.puravida.modules.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.notifications.application.port.in.OrderNotificationPort;
import com.puravida.modules.orders.application.dto.CreateOrderItemRequest;
import com.puravida.modules.orders.application.dto.CreateOrderRequest;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.out.BusinessStatusForOrderRepositoryPort;
import com.puravida.modules.orders.application.port.out.MenuForOrderRepositoryPort;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderItem;
import com.puravida.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private MenuForOrderRepositoryPort menuRepositoryPort;

    @Mock
    private BusinessStatusForOrderRepositoryPort businessStatusRepositoryPort;

    @Mock
    private OrderAuthorizationService authorizationService;

    @Mock
    private OrderResponseAssembler responseAssembler;

    @Mock
    private OrderNotificationPort orderNotificationPort;

    @InjectMocks
    private CreateOrderUseCase useCase;

    @Test
    void createsPendingOrderUsingCurrentMenuPrice() {
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new CreateOrderItemRequest(20, 2)),
                " Sin cebolla "
        );
        Order savedOrder = TestOrderData.pendingOrder();
        OrderItem savedItem = TestOrderData.orderItem();
        OrderResponse expectedResponse = response(savedOrder, List.of(savedItem));

        when(authorizationService.requireCliente(TestOrderData.authenticatedClient())).thenReturn(TestOrderData.client());
        when(businessStatusRepositoryPort.findOpenByFecha(any())).thenReturn(Optional.of(true));
        when(menuRepositoryPort.findByFecha(any())).thenReturn(List.of(TestOrderData.menuItem()));
        when(orderRepositoryPort.save(any(Order.class))).thenReturn(savedOrder);
        when(orderRepositoryPort.saveItems(any())).thenReturn(List.of(savedItem));
        when(responseAssembler.detail(savedOrder, List.of(savedItem))).thenReturn(expectedResponse);

        OrderResponse response = useCase.create(request, TestOrderData.authenticatedClient());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepositoryPort).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().total()).isEqualByComparingTo(new BigDecimal("130.00"));
        assertThat(orderCaptor.getValue().estado().databaseValue()).isEqualTo("pendiente");
        assertThat(orderCaptor.getValue().observaciones()).isEqualTo("Sin cebolla");
        verify(orderNotificationPort).notifyOrderCreated(10);
        assertThat(response).isSameAs(expectedResponse);
    }

    @Test
    void rejectsOrderWhenBusinessIsClosed() {
        when(authorizationService.requireCliente(TestOrderData.authenticatedClient())).thenReturn(TestOrderData.client());
        when(businessStatusRepositoryPort.findOpenByFecha(any())).thenReturn(Optional.of(false));

        assertThatThrownBy(() -> useCase.create(
                new CreateOrderRequest(List.of(new CreateOrderItemRequest(20, 1)), null),
                TestOrderData.authenticatedClient()
        )).isInstanceOf(ConflictException.class);

        verify(orderRepositoryPort, never()).save(any());
        verify(orderNotificationPort, never()).notifyOrderCreated(any());
    }

    private OrderResponse response(Order order, List<OrderItem> items) {
        return OrderResponse.from(order, "Cliente Prueba", items);
    }
}
