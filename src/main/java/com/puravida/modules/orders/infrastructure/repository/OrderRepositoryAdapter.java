package com.puravida.modules.orders.infrastructure.repository;

import com.puravida.modules.orders.application.port.out.OrderRepositoryPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderItem;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.orders.infrastructure.persistence.OrderEntity;
import com.puravida.modules.orders.infrastructure.persistence.OrderItemEntity;
import com.puravida.modules.orders.infrastructure.persistence.OrderItemJpaRepository;
import com.puravida.modules.orders.infrastructure.persistence.OrderJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;

    public OrderRepositoryAdapter(
            OrderJpaRepository orderJpaRepository,
            OrderItemJpaRepository orderItemJpaRepository
    ) {
        this.orderJpaRepository = orderJpaRepository;
        this.orderItemJpaRepository = orderItemJpaRepository;
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(OrderEntity.fromDomain(order)).toDomain();
    }

    @Override
    public List<OrderItem> saveItems(List<OrderItem> items) {
        return orderItemJpaRepository.saveAll(items.stream().map(OrderItemEntity::fromDomain).toList())
                .stream()
                .map(OrderItemEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Order> findById(Integer orderId) {
        return orderJpaRepository.findById(orderId).map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findByIdAndClientId(Integer orderId, Integer clientId) {
        return orderJpaRepository.findByIdAndClienteId(orderId, clientId).map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findByIdForUpdate(Integer orderId) {
        return orderJpaRepository.findByIdForUpdate(orderId).map(OrderEntity::toDomain);
    }

    @Override
    public List<OrderItem> findItemsByOrderId(Integer orderId) {
        return orderItemJpaRepository.findByOrderIdOrderByIdAsc(orderId).stream()
                .map(OrderItemEntity::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByClientId(Integer clientId) {
        return orderJpaRepository.findByClienteIdOrderByFechaDescHoraDesc(clientId).stream()
                .map(OrderEntity::toDomain)
                .toList();
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAllByOrderByFechaDescHoraDesc().stream()
                .map(OrderEntity::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderJpaRepository.findByEstadoOrderByFechaDescHoraDesc(status).stream()
                .map(OrderEntity::toDomain)
                .toList();
    }
}
