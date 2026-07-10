package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.in.AcceptOrderPort;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcceptOrderUseCase implements AcceptOrderPort {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderAuthorizationService authorizationService;
    private final OrderResponseAssembler responseAssembler;

    public AcceptOrderUseCase(
            OrderRepositoryPort orderRepositoryPort,
            OrderAuthorizationService authorizationService,
            OrderResponseAssembler responseAssembler
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.authorizationService = authorizationService;
        this.responseAssembler = responseAssembler;
    }

    @Override
    @Transactional
    public OrderResponse accept(Integer orderId, AuthenticatedUser authenticatedUser) {
        User actor = authorizationService.requireEncargada(authenticatedUser);
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new NotFoundException("No se encontro el pedido."));
        requirePending(order);

        Order acceptedOrder = orderRepositoryPort.save(order.accept(actor.id()));
        return responseAssembler.detail(
                acceptedOrder,
                orderRepositoryPort.findItemsByOrderId(acceptedOrder.id())
        );
    }

    private void requirePending(Order order) {
        if (order.estado() != OrderStatus.PENDIENTE) {
            throw new ConflictException("Solo los pedidos pendientes pueden aceptarse.");
        }
    }
}
