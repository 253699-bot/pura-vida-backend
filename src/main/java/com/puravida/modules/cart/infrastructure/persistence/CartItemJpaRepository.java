package com.puravida.modules.cart.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemJpaRepository extends JpaRepository<CartItemJpaEntity, Integer> {

    List<CartItemJpaEntity> findByUserIdOrderByCreadoEnAsc(Integer userId);

    Optional<CartItemJpaEntity> findByUserIdAndDishId(Integer userId, Integer dishId);
}
