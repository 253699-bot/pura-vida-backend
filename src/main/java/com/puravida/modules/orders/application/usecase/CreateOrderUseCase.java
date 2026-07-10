package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.orders.application.dto.CreateOrderItemRequest;
import com.puravida.modules.orders.application.dto.CreateOrderRequest;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.in.CreateOrderPort;
import com.puravida.modules.orders.application.port.out.BusinessStatusForOrderRepositoryPort;
import com.puravida.modules.orders.application.port.out.MenuForOrderRepositoryPort;
import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.exception.OrderValidationException;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderItem;
import com.puravida.modules.orders.domain.model.OrderableMenuItem;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOrderUseCase implements CreateOrderPort {

    private final OrderRepositoryPort orderRepositoryPort;
    private final MenuForOrderRepositoryPort menuRepositoryPort;
    private final BusinessStatusForOrderRepositoryPort businessStatusRepositoryPort;
    private final OrderAuthorizationService authorizationService;
    private final OrderResponseAssembler responseAssembler;

    public CreateOrderUseCase(
            OrderRepositoryPort orderRepositoryPort,
            MenuForOrderRepositoryPort menuRepositoryPort,
            BusinessStatusForOrderRepositoryPort businessStatusRepositoryPort,
            OrderAuthorizationService authorizationService,
            OrderResponseAssembler responseAssembler
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.menuRepositoryPort = menuRepositoryPort;
        this.businessStatusRepositoryPort = businessStatusRepositoryPort;
        this.authorizationService = authorizationService;
        this.responseAssembler = responseAssembler;
    }

    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest request, AuthenticatedUser authenticatedUser) {
        User client = authorizationService.requireCliente(authenticatedUser);
        validateRequest(request);

        LocalDate today = LocalDate.now();
        validateBusinessIsOpen(today);
        Map<Integer, OrderableMenuItem> menuItemsById = menuItemsForToday(today);
        List<OrderItem> itemsWithoutOrder = toOrderItems(request.items(), menuItemsById, today);
        BigDecimal total = itemsWithoutOrder.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order savedOrder = orderRepositoryPort.save(Order.create(
                client.id(),
                today,
                LocalTime.now(),
                total,
                normalizeNotes(request.notas())
        ));
        List<OrderItem> savedItems = orderRepositoryPort.saveItems(itemsWithoutOrder.stream()
                .map(item -> new OrderItem(
                        item.id(),
                        savedOrder.id(),
                        item.dishId(),
                        item.menuItemId(),
                        item.nombrePlatillo(),
                        item.cantidad(),
                        item.precioUnitario(),
                        item.subtotal()
                ))
                .toList());

        return responseAssembler.detail(savedOrder, savedItems);
    }

    private void validateRequest(CreateOrderRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new OrderValidationException("El pedido debe contener al menos un item.");
        }

        Set<Integer> menuItemIds = new HashSet<>();
        for (CreateOrderItemRequest item : request.items()) {
            if (item == null || item.menuItemId() == null || item.cantidad() == null || item.cantidad() <= 0) {
                throw new OrderValidationException("Cada item debe tener un menuItemId y una cantidad positiva.");
            }
            if (!menuItemIds.add(item.menuItemId())) {
                throw new OrderValidationException("No se permiten items de menu duplicados en un pedido.");
            }
        }
    }

    private void validateBusinessIsOpen(LocalDate today) {
        Boolean isOpen = businessStatusRepositoryPort.findOpenByFecha(today)
                .orElseThrow(() -> new ConflictException("El estado de la fonda aun no ha sido configurado para hoy."));
        if (!isOpen) {
            throw new ConflictException("La fonda esta cerrada y no puede recibir pedidos.");
        }
    }

    private Map<Integer, OrderableMenuItem> menuItemsForToday(LocalDate today) {
        List<OrderableMenuItem> menuItems = menuRepositoryPort.findByFecha(today);
        if (menuItems.isEmpty()) {
            throw new ConflictException("No hay menu configurado para hoy.");
        }

        return menuItems.stream().collect(Collectors.toMap(OrderableMenuItem::id, Function.identity()));
    }

    private List<OrderItem> toOrderItems(
            List<CreateOrderItemRequest> requestedItems,
            Map<Integer, OrderableMenuItem> menuItemsById,
            LocalDate today
    ) {
        return requestedItems.stream().map(requestedItem -> {
            OrderableMenuItem menuItem = menuItemsById.get(requestedItem.menuItemId());
            if (menuItem == null) {
                throw new NotFoundException("Uno de los items no pertenece al menu de hoy.");
            }
            if (!today.equals(menuItem.fecha())) {
                throw new ConflictException("Uno de los items no pertenece al menu de hoy.");
            }
            if (!menuItem.dishActivo() || !menuItem.disponible()) {
                throw new ConflictException("Uno de los items seleccionados no esta disponible.");
            }
            return OrderItem.create(null, menuItem, requestedItem.cantidad());
        }).toList();
    }

    private String normalizeNotes(String notes) {
        return notes == null || notes.isBlank() ? null : notes.trim();
    }
}
