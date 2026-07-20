package com.puravida.modules.orders.infrastructure.persistence;

import com.puravida.modules.orders.domain.model.OrderStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT entity FROM OrderEntity entity WHERE entity.id = :id")
    Optional<OrderEntity> findByIdForUpdate(@Param("id") Integer id);

    Optional<OrderEntity> findByIdAndClienteId(Integer id, Integer clienteId);

    List<OrderEntity> findByClienteIdOrderByFechaDescHoraDesc(Integer clienteId);

    List<OrderEntity> findByEstadoOrderByFechaDescHoraDesc(OrderStatus estado);

    List<OrderEntity> findByEstadoInOrderByFechaDescHoraDesc(List<OrderStatus> estados);

    List<OrderEntity> findAllByOrderByFechaDescHoraDesc();

    List<OrderEntity> findByCreadoEnGreaterThanEqualOrderByFechaDescHoraDesc(LocalDateTime creadoEn);

    List<OrderEntity> findByEstadoAndCreadoEnGreaterThanEqualOrderByFechaDescHoraDesc(
            OrderStatus estado,
            LocalDateTime creadoEn
    );

    long countByEstadoAndCreadoEnGreaterThanEqual(OrderStatus estado, LocalDateTime creadoEn);
}