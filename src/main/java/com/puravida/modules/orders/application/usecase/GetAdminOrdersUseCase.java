package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.business.application.port.out.BusinessDayStatusRepositoryPort;
import com.puravida.modules.business.domain.model.BusinessDayStatus;
import com.puravida.modules.orders.application.dto.OrderSummaryResponse;
import com.puravida.modules.orders.application.port.in.GetAdminOrdersPort;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAdminOrdersUseCase implements GetAdminOrdersPort {

    private static final Set<OrderStatus> HISTORICAL_STATUSES = EnumSet.of(
            OrderStatus.FINALIZADO,
            OrderStatus.RECHAZADO,
            OrderStatus.CANCELADO
    );

    private final OrderRepositoryPort orderRepositoryPort;
    private final BusinessDayStatusRepositoryPort businessDayStatusRepositoryPort;
    private final OrderAuthorizationService authorizationService;
    private final OrderResponseAssembler responseAssembler;

    public GetAdminOrdersUseCase(
            OrderRepositoryPort orderRepositoryPort,
            BusinessDayStatusRepositoryPort businessDayStatusRepositoryPort,
            OrderAuthorizationService authorizationService,
            OrderResponseAssembler responseAssembler
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.businessDayStatusRepositoryPort = businessDayStatusRepositoryPort;
        this.authorizationService = authorizationService;
        this.responseAssembler = responseAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrders(
            String estado,
            boolean currentCycleOnly,
            boolean historyOnly,
            AuthenticatedUser authenticatedUser
    ) {
        authorizationService.requireEncargada(authenticatedUser);
        java.util.Optional<OrderStatus> parsedStatus = parseStatus(estado);
        List<Order> orders = findOrders(parsedStatus, currentCycleOnly, historyOnly);

        return orders.stream()
                .map(responseAssembler::summary)
                .toList();
    }

    private List<Order> findOrders(
            java.util.Optional<OrderStatus> status,
            boolean currentCycleOnly,
            boolean historyOnly
    ) {
        if (currentCycleOnly) {
            return findCurrentCycleOrders(status);
        }

        if (historyOnly) {
            return findHistoricalOrders(status);
        }

        return status.map(orderRepositoryPort::findByStatus).orElseGet(orderRepositoryPort::findAll);
    }

    private List<Order> findHistoricalOrders(java.util.Optional<OrderStatus> status) {
        if (status.isPresent()) {
            OrderStatus value = status.get();
            return HISTORICAL_STATUSES.contains(value)
                    ? orderRepositoryPort.findByStatus(value)
                    : List.of();
        }

        return orderRepositoryPort.findByStatuses(List.copyOf(HISTORICAL_STATUSES));
    }

    private List<Order> findCurrentCycleOrders(java.util.Optional<OrderStatus> status) {
        return businessDayStatusRepositoryPort.findByFecha(LocalDate.now())
                .filter(BusinessDayStatus::abierto)
                .map(BusinessDayStatus::cicloIniciadoEn)
                .filter(cycleStartedAt -> cycleStartedAt != null)
                .map(cycleStartedAt -> findSinceCycleStart(status, cycleStartedAt))
                .orElseGet(List::of);
    }

    private List<Order> findSinceCycleStart(java.util.Optional<OrderStatus> status, LocalDateTime cycleStartedAt) {
        return status
                .map(value -> orderRepositoryPort.findByStatusCreatedSince(value, cycleStartedAt))
                .orElseGet(() -> orderRepositoryPort.findCreatedSince(cycleStartedAt));
    }

    private java.util.Optional<OrderStatus> parseStatus(String estado) {
        if (estado == null || estado.isBlank()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(OrderStatus.fromDatabaseValue(estado.trim().toLowerCase(Locale.ROOT)));
    }
}