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
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RejectOrderUseCase implements RejectOrderPort {

    private static final Set<String> VALID_REJECTION_CATEGORIES = Set.of(
            "platillo_agotado",
            "fonda_cerrada",
            "pedido_fuera_de_horario",
            "cantidad_no_disponible",
            "otro"
    );
    private static final String OTHER_REJECTION_CATEGORY = "otro";

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
        String categoriaRechazo = normalizeCategory(request);
        String motivoRechazo = normalizeReason(request, categoriaRechazo);
        Order order = orderRepositoryPort.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("No se encontro el pedido."));
        requirePending(order);

        Order rejectedOrder = orderRepositoryPort.save(order.reject(actor.id(), categoriaRechazo, motivoRechazo));
        orderNotificationPort.notifyOrderRejected(
                rejectedOrder.id(),
                rejectedOrder.clienteId(),
                rejectedOrder.categoriaRechazo(),
                rejectedOrder.motivoRechazo()
        );
        return responseAssembler.detail(
                rejectedOrder,
                orderRepositoryPort.findItemsByOrderId(rejectedOrder.id())
        );
    }

    private String normalizeCategory(RejectOrderRequest request) {
        if (request == null || request.categoriaRechazo() == null || request.categoriaRechazo().isBlank()) {
            throw new OrderValidationException("La categoria de rechazo es obligatoria.");
        }
        String category = request.categoriaRechazo().trim();
        if (!VALID_REJECTION_CATEGORIES.contains(category)) {
            throw new OrderValidationException("La categoria de rechazo no es valida.");
        }
        return category;
    }

    private String normalizeReason(RejectOrderRequest request, String category) {
        if (OTHER_REJECTION_CATEGORY.equals(category)
                && (request.motivoRechazo() == null || request.motivoRechazo().isBlank())) {
            throw new OrderValidationException("El motivo de rechazo es obligatorio para la categoria otro.");
        }
        if (request.motivoRechazo() == null || request.motivoRechazo().isBlank()) {
            return null;
        }
        return request.motivoRechazo().trim();
    }

    private void requirePending(Order order) {
        if (order.estado() != OrderStatus.PENDIENTE) {
            throw new ConflictException("Solo los pedidos pendientes pueden rechazarse.");
        }
    }
}
