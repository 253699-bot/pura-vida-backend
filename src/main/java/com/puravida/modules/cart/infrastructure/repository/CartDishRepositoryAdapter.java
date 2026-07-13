package com.puravida.modules.cart.infrastructure.repository;

import com.puravida.modules.cart.application.port.out.CartDishRepositoryPort;
import com.puravida.modules.cart.domain.model.CartDish;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.menu.infrastructure.persistence.DishEntity;
import com.puravida.modules.menu.infrastructure.persistence.DishJpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CartDishRepositoryAdapter implements CartDishRepositoryPort {

    private final DishJpaRepository dishJpaRepository;

    public CartDishRepositoryAdapter(DishJpaRepository dishJpaRepository) {
        this.dishJpaRepository = dishJpaRepository;
    }

    @Override
    public Optional<CartDish> findById(Integer dishId) {
        return dishJpaRepository.findById(dishId).map(this::toCartDish);
    }

    @Override
    public List<CartDish> findAllByIds(Collection<Integer> dishIds) {
        return dishJpaRepository.findAllById(dishIds).stream().map(this::toCartDish).toList();
    }

    private CartDish toCartDish(DishEntity dish) {
        Dish domainDish = dish.toDomain();
        return new CartDish(
                domainDish.id(),
                domainDish.nombre(),
                domainDish.precioBase(),
                domainDish.activo()
        );
    }
}
