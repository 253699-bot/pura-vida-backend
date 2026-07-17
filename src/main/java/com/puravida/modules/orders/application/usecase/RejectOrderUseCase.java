package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.notifications.application.port.in.OrderNotificationPort;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.dto.RejectOrderRequest;
import com.puravida.modules.orders.application.port.in.RejectOrderPort;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.exception.OrderValidationException;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RejectOrderUseCase implements RejectOrderPort {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderAuthorizationService authorizationService;
    private final OrderResponseAssembler responseAssembler;
    private final OrderNotificationPort orderNotificationPort;

    public RejectOrderUseCase(
            OrderRepositoryPort orderRepositoryPort,
            OrderAuthorizationService authorizationService,
            OrderResponseAssembler responseAssembler,
            OrderNotificationPort orderNotificationPort
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.authorizationService = authorizationService;
        this.responseAssembler = responseAssembler;
        this.orderNotificationPort = orderNotificationPort;
    }

    @Override
    @Transactional
    public OrderResponse reject(
            Integer orderId,
            RejectOrderRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        User actor = authorizationService.requireEncargada(authenticatedUser);
        String motivoRechazo = normalizeReason(request);
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new NotFoundException("No se encontro el pedido."));
        requirePending(order);

        Order rejectedOrder = orderRepositoryPort.save(order.reject(actor.id(), motivoRechazo));
        orderNotificationPort.notifyOrderRejected(
                rejectedOrder.id(),
                rejectedOrder.clienteId(),
                rejectedOrder.motivoRechazo()
        );
        return responseAssembler.detail(
                rejectedOrder,
                orderRepositoryPort.findItemsByOrderId(rejectedOrder.id())
        );
    }

    private String normalizeReason(RejectOrderRequest request) {
        if (request == null || request.motivoRechazo() == null || request.motivoRechazo().isBlank()) {
            throw new OrderValidationException("El motivo de rechazo es obligatorio.");
        }
        return request.motivoRechazo().trim();
    }

    private void requirePending(Order order) {
        if (order.estado() != OrderStatus.PENDIENTE) {
            throw new ConflictException("Solo los pedidos pendientes pueden rechazarse.");
        }
    }
}
