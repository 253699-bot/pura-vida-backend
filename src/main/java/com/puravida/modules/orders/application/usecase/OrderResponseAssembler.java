package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.menu.application.dto.DishResponse;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.orders.application.dto.OrderItemResponse;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.dto.OrderSummaryResponse;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderItem;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderResponseAssembler {

    private final UserRepositoryPort userRepositoryPort;
    private final DishRepositoryPort dishRepositoryPort;

    public OrderResponseAssembler(UserRepositoryPort userRepositoryPort, DishRepositoryPort dishRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.dishRepositoryPort = dishRepositoryPort;
    }

    public OrderSummaryResponse summary(Order order) {
        return OrderSummaryResponse.from(order, clientName(order.clienteId()));
    }

    public OrderResponse detail(Order order, List<OrderItem> items) {
        Map<Integer, Dish> dishesById = dishRepositoryPort.findAllByIds(items.stream()
                        .map(OrderItem::dishId)
                        .filter(id -> id != null)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Dish::id, Function.identity()));

        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> OrderItemResponse.from(item, imageUrlFor(dishesById.get(item.dishId()))))
                .toList();

        return OrderResponse.fromResponses(order, clientName(order.clienteId()), itemResponses);
    }

    private String imageUrlFor(Dish dish) {
        return dish == null ? null : DishResponse.publicImageUrl(dish);
    }

    private String clientName(Integer clientId) {
        if (clientId == null) {
            return null;
        }

        return userRepositoryPort.findById(clientId)
                .map(user -> user.nombre())
                .orElse(null);
    }
}