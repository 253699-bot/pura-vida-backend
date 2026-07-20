package com.puravida.modules.cart.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.cart.application.port.in.CheckoutCartPort;
import com.puravida.modules.cart.application.port.out.CartRepositoryPort;
import com.puravida.modules.cart.domain.model.CartItem;
import com.puravida.modules.orders.application.dto.CreateOrderItemRequest;
import com.puravida.modules.orders.application.dto.CreateOrderRequest;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.port.in.CreateOrderPort;
import com.puravida.modules.orders.application.port.out.MenuForOrderRepositoryPort;
import com.puravida.modules.orders.domain.model.OrderableMenuItem;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ConflictException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutCartUseCase implements CheckoutCartPort {

    private final CartRepositoryPort cartRepositoryPort;
    private final MenuForOrderRepositoryPort menuRepositoryPort;
    private final CreateOrderPort createOrderPort;
    private final CartAuthorizationService authorizationService;

    public CheckoutCartUseCase(
            CartRepositoryPort cartRepositoryPort,
            MenuForOrderRepositoryPort menuRepositoryPort,
            CreateOrderPort createOrderPort,
            CartAuthorizationService authorizationService
    ) {
        this.cartRepositoryPort = cartRepositoryPort;
        this.menuRepositoryPort = menuRepositoryPort;
        this.createOrderPort = createOrderPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public OrderResponse checkout(AuthenticatedUser authenticatedUser) {
        User client = authorizationService.requireClient(authenticatedUser);
        List<CartItem> cartItems = cartRepositoryPort.findByUserIdForUpdate(client.id());
        if (cartItems.isEmpty()) {
            throw new ConflictException("El carrito esta vacio.");
        }

        LocalDate today = LocalDate.now();
        Map<Integer, OrderableMenuItem> menuItemsByDishId = menuItemsByDishId(today);
        List<CreateOrderItemRequest> orderItems = cartItems.stream()
                .map(item -> toOrderItem(item, menuItemsByDishId, today))
                .toList();

        OrderResponse order = createOrderPort.create(new CreateOrderRequest(orderItems, null), authenticatedUser);
        cartRepositoryPort.deleteByUserId(client.id());
        return order;
    }

    private Map<Integer, OrderableMenuItem> menuItemsByDishId(LocalDate today) {
        List<OrderableMenuItem> menuItems = menuRepositoryPort.findByFecha(today);
        if (menuItems.isEmpty()) {
            throw new ConflictException("No hay menu configurado para hoy.");
        }
        return menuItems.stream().collect(Collectors.toMap(OrderableMenuItem::dishId, Function.identity()));
    }

    private CreateOrderItemRequest toOrderItem(
            CartItem cartItem,
            Map<Integer, OrderableMenuItem> menuItemsByDishId,
            LocalDate today
    ) {
        OrderableMenuItem menuItem = menuItemsByDishId.get(cartItem.dishId());
        if (menuItem == null || !today.equals(menuItem.fecha())) {
            throw new ConflictException("Uno de los platillos del carrito no pertenece al menu de hoy.");
        }
        if (!menuItem.dishActivo() || !menuItem.disponible()) {
            throw new ConflictException("Uno de los platillos del carrito no esta disponible.");
        }
        return new CreateOrderItemRequest(menuItem.id(), cartItem.cantidad());
    }
}
