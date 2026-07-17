package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.notifications.application.port.in.OrderNotificationPort;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.in.AcceptOrderPort;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcceptOrderUseCase implements AcceptOrderPort {

    private final OrderRepositoryPort orderRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;
    private final OrderAuthorizationService authorizationService;
    private final OrderResponseAssembler responseAssembler;
    private final OrderNotificationPort orderNotificationPort;

    public AcceptOrderUseCase(
            OrderRepositoryPort orderRepositoryPort,
            SaleRepositoryPort saleRepositoryPort,
            OrderAuthorizationService authorizationService,
            OrderResponseAssembler responseAssembler,
            OrderNotificationPort orderNotificationPort
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.saleRepositoryPort = saleRepositoryPort;
        this.authorizationService = authorizationService;
        this.responseAssembler = responseAssembler;
        this.orderNotificationPort = orderNotificationPort;
    }

    @Override
    @Transactional
    public OrderResponse accept(Integer orderId, AuthenticatedUser authenticatedUser) {
        User actor = authorizationService.requireEncargada(authenticatedUser);
        Order order = orderRepositoryPort.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("No se encontro el pedido."));

        if (order.estado() == OrderStatus.ACEPTADO) {
            if (saleRepositoryPort.findByOrderId(order.id()).isPresent()) {
                return responseAssembler.detail(order, orderRepositoryPort.findItemsByOrderId(order.id()));
            }
            throw new ConflictException("El pedido esta aceptado pero no tiene una venta asociada.");
        }

        requirePending(order);
        if (saleRepositoryPort.existsByOrderId(order.id())) {
            throw new ConflictException("El pedido ya tiene una venta asociada y no puede aceptarse nuevamente.");
        }

        Order acceptedOrder = orderRepositoryPort.save(order.accept(actor.id()));
        saleRepositoryPort.save(Sale.createRemote(acceptedOrder.id(), acceptedOrder.total(), actor.id()));
        orderNotificationPort.notifyOrderAccepted(acceptedOrder.id(), acceptedOrder.clienteId());
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
