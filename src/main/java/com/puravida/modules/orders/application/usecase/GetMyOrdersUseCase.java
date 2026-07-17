package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.orders.application.dto.OrderSummaryResponse;
import com.puravida.modules.orders.application.port.in.GetMyOrdersPort;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMyOrdersUseCase implements GetMyOrdersPort {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderAuthorizationService authorizationService;
    private final OrderResponseAssembler responseAssembler;

    public GetMyOrdersUseCase(
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
    public List<OrderSummaryResponse> getMyOrders(AuthenticatedUser authenticatedUser) {
        User actor = authorizationService.requireClienteForHistory(authenticatedUser);
        return orderRepositoryPort.findByClientId(actor.id()).stream()
                .map(responseAssembler::summary)
                .toList();
    }
}
