package com.puravida.modules.cart.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemJpaRepository extends JpaRepository<CartItemJpaEntity, Integer> {

    List<CartItemJpaEntity> findByUserIdOrderByCreadoEnAsc(Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM CartItemJpaEntity item WHERE item.userId = :userId ORDER BY item.creadoEn ASC")
    List<CartItemJpaEntity> findByUserIdForUpdate(@Param("userId") Integer userId);

    Optional<CartItemJpaEntity> findByUserIdAndDishId(Integer userId, Integer dishId);

    long deleteByUserId(Integer userId);
}
