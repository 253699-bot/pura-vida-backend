package com.puravida.modules.orders.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, Integer> {

    List<OrderItemEntity> findByOrderIdOrderByIdAsc(Integer orderId);
}
