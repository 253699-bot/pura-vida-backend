package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.in.CompleteOrderPort;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.modules.sales.domain.model.SaleSource;
import com.puravida.modules.sales.domain.model.SaleStatus;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.util.Optional;
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
        User actor = authorizationService.requireEncargada(authenticatedUser);
        Order order = orderRepositoryPort.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("No se encontro el pedido."));

        if (order.estado() == OrderStatus.FINALIZADO) {
            Sale sale = saleRepositoryPort.findByOrderIdForUpdate(order.id())
                    .orElseThrow(() -> new ConflictException(
                            "El pedido finalizado no tiene una venta remota asociada."
                    ));
            requireActiveRemoteSale(sale);
            return detail(order);
        }

        if (order.estado() != OrderStatus.ACEPTADO) {
            throw new ConflictException("Solo los pedidos aceptados pueden finalizarse.");
        }

        Optional<Sale> existingSale = saleRepositoryPort.findByOrderIdForUpdate(order.id());
        existingSale.ifPresent(this::requireActiveRemoteSale);

        Order completedOrder = orderRepositoryPort.save(order.complete());
        if (existingSale.isEmpty()) {
            saleRepositoryPort.save(Sale.createRemote(completedOrder.id(), completedOrder.total(), actor.id()));
        }
        return detail(completedOrder);
    }

    private void requireActiveRemoteSale(Sale sale) {
        if (sale.source() != SaleSource.REMOTA) {
            throw new ConflictException("La venta asociada al pedido no es remota.");
        }
        if (sale.status() != SaleStatus.ACTIVA) {
            throw new ConflictException("La venta remota asociada no esta activa.");
        }
    }

    private OrderResponse detail(Order order) {
        return responseAssembler.detail(
                order,
                orderRepositoryPort.findItemsByOrderId(order.id())
        );
    }
}