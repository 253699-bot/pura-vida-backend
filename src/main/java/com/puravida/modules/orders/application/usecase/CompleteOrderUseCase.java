package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.in.CompleteOrderPort;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteOrderUseCase implements CompleteOrderPort {

    private final OrderRepositoryPort orderRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;
    private final OrderAuthorizationService authorizationService;
    private final OrderResponseAssembler responseAssembler;

    public CompleteOrderUseCase(
            OrderRepositoryPort orderRepositoryPort,
            SaleRepositoryPort saleRepositoryPort,
            OrderAuthorizationService authorizationService,
            OrderResponseAssembler responseAssembler
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.saleRepositoryPort = saleRepositoryPort;
        this.authorizationService = authorizationService;
        this.responseAssembler = responseAssembler;
    }

    @Override
    @Transactional
    public OrderResponse complete(Integer orderId, AuthenticatedUser authenticatedUser) {
        authorizationService.requireEncargada(authenticatedUser);
        Order order = orderRepositoryPort.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("No se encontro el pedido."));

        if (order.estado() != OrderStatus.ACEPTADO) {
            throw new ConflictException("Solo los pedidos aceptados pueden finalizarse.");
        }
        if (!saleRepositoryPort.existsByOrderId(order.id())) {
            throw new ConflictException("El pedido aceptado no tiene una venta asociada y no puede finalizarse.");
        }

        Order completedOrder = orderRepositoryPort.save(order.complete());
        return responseAssembler.detail(
                completedOrder,
                orderRepositoryPort.findItemsByOrderId(completedOrder.id())
        );
    }
}
