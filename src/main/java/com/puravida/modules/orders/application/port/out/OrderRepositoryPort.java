package com.puravida.modules.orders.application.port.out;

import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderItem;
import com.puravida.modules.orders.domain.model.OrderStatus;
import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    List<OrderItem> saveItems(List<OrderItem> items);

    Optional<Order> findById(Integer orderId);

    Optional<Order> findByIdForUpdate(Integer orderId);

    List<OrderItem> findItemsByOrderId(Integer orderId);

    List<Order> findByClientId(Integer clientId);

    List<Order> findAll();

    List<Order> findByStatus(OrderStatus status);
}
