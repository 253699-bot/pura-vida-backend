package com.puravida.modules.orders.infrastructure.persistence;

import com.puravida.modules.orders.domain.model.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Integer> {

    List<OrderEntity> findByClienteIdOrderByFechaDescHoraDesc(Integer clienteId);

    List<OrderEntity> findByEstadoOrderByFechaDescHoraDesc(OrderStatus estado);

    List<OrderEntity> findAllByOrderByFechaDescHoraDesc();
}
