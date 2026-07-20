package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.in.GetAdminOrderDetailPort;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAdminOrderDetailUseCase implements GetAdminOrderDetailPort {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderAuthorizationService authorizationService;
    private final OrderResponseAssembler responseAssembler;

    public GetAdminOrderDetailUseCase(
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
    public OrderResponse getOrder(Integer orderId, AuthenticatedUser authenticatedUser) {
        authorizationService.requireEncargada(authenticatedUser);
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new NotFoundException("No se encontro el pedido."));
        return responseAssembler.detail(order, orderRepositoryPort.findItemsByOrderId(order.id()));
    }
}
