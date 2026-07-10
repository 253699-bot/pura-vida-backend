package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.dto.OrderSummaryResponse;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderItem;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderResponseAssembler {

    private final UserRepositoryPort userRepositoryPort;

    public OrderResponseAssembler(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public OrderSummaryResponse summary(Order order) {
        return OrderSummaryResponse.from(order, clientName(order.clienteId()));
    }

    public OrderResponse detail(Order order, List<OrderItem> items) {
        return OrderResponse.from(order, clientName(order.clienteId()), items);
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
