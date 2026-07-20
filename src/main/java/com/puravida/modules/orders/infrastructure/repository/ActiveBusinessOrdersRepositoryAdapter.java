package com.puravida.modules.orders.infrastructure.repository;

import com.puravida.modules.business.application.port.out.ActiveBusinessOrdersPort;
import com.puravida.modules.business.domain.model.ActiveBusinessOrderCounts;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.orders.infrastructure.persistence.OrderJpaRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

@Repository
public class ActiveBusinessOrdersRepositoryAdapter implements ActiveBusinessOrdersPort {

    private final OrderJpaRepository orderJpaRepository;

    public ActiveBusinessOrdersRepositoryAdapter(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public ActiveBusinessOrderCounts countActiveSince(LocalDateTime cycleStartedAt) {
        long pending = orderJpaRepository.countByEstadoAndCreadoEnGreaterThanEqual(
                OrderStatus.PENDIENTE,
                cycleStartedAt
        );
        long accepted = orderJpaRepository.countByEstadoAndCreadoEnGreaterThanEqual(
                OrderStatus.ACEPTADO,
                cycleStartedAt
        );
        return new ActiveBusinessOrderCounts(pending, accepted);
    }
}