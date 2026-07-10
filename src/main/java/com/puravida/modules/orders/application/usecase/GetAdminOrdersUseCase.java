package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.orders.application.dto.OrderSummaryResponse;
import com.puravida.modules.orders.application.port.in.GetAdminOrdersPort;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.OrderStatus;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAdminOrdersUseCase implements GetAdminOrdersPort {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderAuthorizationService authorizationService;
    private final OrderResponseAssembler responseAssembler;

    public GetAdminOrdersUseCase(
            OrderRepositoryPort orderRepositoryPort,
            OrderAuthorizationService authorizationService,
            OrderResponseAssembler responseAssembler
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.authorizationService = authorizationService;
        this.responseAssembler = responseAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getOrders(String estado, AuthenticatedUser authenticatedUser) {
        authorizationService.requireEncargada(authenticatedUser);
        return parseStatus(estado)
                .map(orderRepositoryPort::findByStatus)
                .orElseGet(orderRepositoryPort::findAll)
                .stream()
                .map(responseAssembler::summary)
                .toList();
    }

    private java.util.Optional<OrderStatus> parseStatus(String estado) {
        if (estado == null || estado.isBlank()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(OrderStatus.fromDatabaseValue(estado.trim().toLowerCase(Locale.ROOT)));
    }
}
