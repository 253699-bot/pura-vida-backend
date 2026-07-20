package com.puravida.modules.orders.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.business.application.port.out.BusinessDayStatusRepositoryPort;
import com.puravida.modules.business.domain.model.BusinessDayStatus;
import com.puravida.modules.orders.application.dto.OrderSummaryResponse;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAdminOrdersUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private BusinessDayStatusRepositoryPort businessDayStatusRepositoryPort;

    @Mock
    private OrderAuthorizationService authorizationService;

    @Mock
    private OrderResponseAssembler responseAssembler;

    @InjectMocks
    private GetAdminOrdersUseCase useCase;

    @Test
    void currentCycleReturnsEmptyWhenBusinessIsClosed() {
        LocalDate today = LocalDate.now();
        when(businessDayStatusRepositoryPort.findByFecha(today))
                .thenReturn(Optional.of(new BusinessDayStatus(
                        1,
                        today,
                        false,
                        "Fin de jornada",
                        2,
                        LocalDateTime.now().minusHours(4),
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now().minusHours(4)
                )));

        List<OrderSummaryResponse> result = useCase.getOrders(null, true, false, TestOrderData.authenticatedEncargada());

        assertThat(result).isEmpty();
        verify(orderRepositoryPort, never()).findAll();
    }

    @Test
    void currentCycleUsesStatusFilterSinceCycleStart() {
        LocalDate today = LocalDate.now();
        LocalDateTime cycleStartedAt = LocalDateTime.now().minusHours(3);
        Order order = TestOrderData.pendingOrder();
        OrderSummaryResponse summary = OrderSummaryResponse.from(order, "Cliente Prueba");
        when(businessDayStatusRepositoryPort.findByFecha(today))
                .thenReturn(Optional.of(new BusinessDayStatus(
                        1,
                        today,
                        true,
                        null,
                        2,
                        cycleStartedAt,
                        null,
                        cycleStartedAt
                )));
        when(orderRepositoryPort.findByStatusCreatedSince(OrderStatus.PENDIENTE, cycleStartedAt))
                .thenReturn(List.of(order));
        when(responseAssembler.summary(order)).thenReturn(summary);

        List<OrderSummaryResponse> result = useCase.getOrders(
                "pendiente",
                true,
                false,
                TestOrderData.authenticatedEncargada()
        );

        assertThat(result).containsExactly(summary);
    }

    @Test
    void historyAllReturnsOnlyClosedStatuses() {
        Order order = TestOrderData.acceptedOrder().complete();
        OrderSummaryResponse summary = OrderSummaryResponse.from(order, "Cliente Prueba");
        when(orderRepositoryPort.findByStatuses(List.of(
                OrderStatus.FINALIZADO,
                OrderStatus.RECHAZADO,
                OrderStatus.CANCELADO
        ))).thenReturn(List.of(order));
        when(responseAssembler.summary(order)).thenReturn(summary);

        List<OrderSummaryResponse> result = useCase.getOrders(null, false, true, TestOrderData.authenticatedEncargada());

        assertThat(result).containsExactly(summary);
        verify(orderRepositoryPort, never()).findAll();
    }

    @Test
    void historyIgnoresPendingAndAcceptedFilters() {
        List<OrderSummaryResponse> result = useCase.getOrders(
                "aceptado",
                false,
                true,
                TestOrderData.authenticatedEncargada()
        );

        assertThat(result).isEmpty();
        verify(orderRepositoryPort, never()).findByStatus(OrderStatus.ACEPTADO);
    }
}