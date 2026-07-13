package com.puravida.modules.cart.application.port.out;

import com.puravida.modules.cart.domain.model.CartDish;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartDishRepositoryPort {

    Optional<CartDish> findById(Integer dishId);

    List<CartDish> findAllByIds(Collection<Integer> dishIds);
}
