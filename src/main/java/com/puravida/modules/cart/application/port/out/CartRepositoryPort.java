package com.puravida.modules.cart.application.port.out;

import com.puravida.modules.cart.domain.model.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartRepositoryPort {

    List<CartItem> findByUserId(Integer userId);

    List<CartItem> findByUserIdForUpdate(Integer userId);

    Optional<CartItem> findById(Integer cartItemId);

    Optional<CartItem> findByUserIdAndDishId(Integer userId, Integer dishId);

    CartItem save(CartItem cartItem);

    void deleteById(Integer cartItemId);

    void deleteByUserId(Integer userId);
}
