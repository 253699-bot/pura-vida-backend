package com.puravida.modules.orders.application.port.out;

import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderItem;
import com.puravida.modules.orders.domain.model.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {

    Order save(Order order);

    List<OrderItem> saveItems(List<OrderItem> items);

    Optional<Order> findById(Integer orderId);

    Optional<Order> findByIdAndClientId(Integer orderId, Integer clientId);

    Optional<Order> findByIdForUpdate(Integer orderId);

    List<OrderItem> findItemsByOrderId(Integer orderId);

    List<Order> findByClientId(Integer clientId);

    List<Order> findAll();

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByStatuses(List<OrderStatus> statuses);

    List<Order> findCreatedSince(LocalDateTime cycleStartedAt);

    List<Order> findByStatusCreatedSince(OrderStatus status, LocalDateTime cycleStartedAt);
}