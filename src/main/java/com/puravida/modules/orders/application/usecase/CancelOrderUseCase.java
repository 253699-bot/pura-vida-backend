package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.notifications.application.port.in.OrderNotificationPort;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.in.CancelOrderPort;
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
public class CancelOrderUseCase implements CancelOrderPort {

    static final String REMOTE_SALE_CANCELLATION_REASON = "Pedido cancelado por la encargada.";

    private final OrderRepositoryPort orderRepositoryPort;
    private final SaleRepositoryPort saleRepositoryPort;
    private final OrderAuthorizationService authorizationService;
    private final OrderResponseAssembler responseAssembler;
    private final OrderNotificationPort orderNotificationPort;

    public CancelOrderUseCase(
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
    public OrderResponse cancel(Integer orderId, AuthenticatedUser authenticatedUser) {
        User actor = authorizationService.requireEncargada(authenticatedUser);
        Order order = orderRepositoryPort.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("No se encontro el pedido."));

        if (order.estado() != OrderStatus.ACEPTADO && order.estado() != OrderStatus.CANCELADO) {
            throw new ConflictException("Solo los pedidos aceptados pueden cancelarse.");
        }

        Optional<Sale> sale = saleRepositoryPort.findByOrderIdForUpdate(order.id());

        if (order.estado() == OrderStatus.CANCELADO) {
            sale.ifPresent(this::requireCoherentCancelledSale);
            return detail(order);
        }

        sale.ifPresent(existingSale -> {
            requireActiveRemoteSale(existingSale);
            saleRepositoryPort.save(existingSale.cancel(REMOTE_SALE_CANCELLATION_REASON, actor.id()));
        });

        Order cancelledOrder = orderRepositoryPort.save(order.cancel(actor.id()));
        orderNotificationPort.notifyOrderCancelled(cancelledOrder.id(), cancelledOrder.clienteId());
        return detail(cancelledOrder);
    }

    private void requireCoherentCancelledSale(Sale sale) {
        if (sale.source() != SaleSource.REMOTA || sale.status() != SaleStatus.ANULADA) {
            throw new ConflictException(
                    "El pedido cancelado no tiene una venta remota anulada coherente."
            );
        }
    }

    private void requireActiveRemoteSale(Sale sale) {
        if (sale.source() != SaleSource.REMOTA) {
            throw new ConflictException("La venta asociada al pedido no es remota.");
        }
        if (sale.status() != SaleStatus.ACTIVA) {
            throw new ConflictException("La venta remota asociada ya esta anulada.");
        }
    }

    private OrderResponse detail(Order order) {
        return responseAssembler.detail(
                order,
                orderRepositoryPort.findItemsByOrderId(order.id())
        );
    }
}