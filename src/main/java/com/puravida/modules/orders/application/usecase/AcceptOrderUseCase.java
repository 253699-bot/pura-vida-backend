package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.notifications.application.port.in.OrderNotificationPort;
import com.puravida.modules.orders.application.dto.AcceptOrderRequest;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.in.AcceptOrderPort;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.exception.OrderValidationException;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcceptOrderUseCase implements AcceptOrderPort {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderAuthorizationService authorizationService;
    private final OrderResponseAssembler responseAssembler;
    private final OrderNotificationPort orderNotificationPort;

    public AcceptOrderUseCase(
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
    public OrderResponse accept(
            Integer orderId,
            AcceptOrderRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        User actor = authorizationService.requireEncargada(authenticatedUser);
        String estimatedWait = normalizeEstimatedWait(request);
        Order order = orderRepositoryPort.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("No se encontro el pedido."));

        if (order.estado() == OrderStatus.ACEPTADO) {
            if (!Objects.equals(order.tiempoEsperaEstimado(), estimatedWait)) {
                throw new ConflictException("El pedido ya fue aceptado con un tiempo de espera diferente.");
            }
            return responseAssembler.detail(order, orderRepositoryPort.findItemsByOrderId(order.id()));
        }

        requirePending(order);
        Order acceptedOrder = orderRepositoryPort.save(order.accept(actor.id(), estimatedWait));
        orderNotificationPort.notifyOrderAccepted(acceptedOrder.id(), acceptedOrder.clienteId());
        return responseAssembler.detail(
                acceptedOrder,
                orderRepositoryPort.findItemsByOrderId(acceptedOrder.id())
        );
    }

    private String normalizeEstimatedWait(AcceptOrderRequest request) {
        if (request == null
                || request.tiempoEsperaEstimado() == null
                || request.tiempoEsperaEstimado().isBlank()) {
            throw new OrderValidationException("El tiempo estimado de espera es obligatorio.");
        }
        String normalized = request.tiempoEsperaEstimado().trim();
        if (normalized.length() > 100) {
            throw new OrderValidationException(
                    "El tiempo estimado de espera no debe exceder 100 caracteres."
            );
        }
        return normalized;
    }

    private void requirePending(Order order) {
        if (order.estado() != OrderStatus.PENDIENTE) {
            throw new ConflictException("Solo los pedidos pendientes pueden aceptarse.");
        }
    }
}