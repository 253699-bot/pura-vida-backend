package com.puravida.modules.cart.infrastructure.repository;

import com.puravida.modules.cart.application.port.out.CartRepositoryPort;
import com.puravida.modules.cart.domain.model.CartItem;
import com.puravida.modules.cart.infrastructure.persistence.CartItemJpaEntity;
import com.puravida.modules.cart.infrastructure.persistence.CartItemJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CartRepositoryAdapter implements CartRepositoryPort {

    private final CartItemJpaRepository cartItemJpaRepository;

    public CartRepositoryAdapter(CartItemJpaRepository cartItemJpaRepository) {
        this.cartItemJpaRepository = cartItemJpaRepository;
    }

    @Override
    public List<CartItem> findByUserId(Integer userId) {
        return cartItemJpaRepository.findByUserIdOrderByCreadoEnAsc(userId).stream()
                .map(CartItemJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<CartItem> findByUserIdForUpdate(Integer userId) {
        return cartItemJpaRepository.findByUserIdForUpdate(userId).stream()
                .map(CartItemJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<CartItem> findById(Integer cartItemId) {
        return cartItemJpaRepository.findById(cartItemId).map(CartItemJpaEntity::toDomain);
    }

    @Override
    public Optional<CartItem> findByIdAndUserId(Integer cartItemId, Integer userId) {
        return cartItemJpaRepository.findByIdAndUserId(cartItemId, userId)
                .map(CartItemJpaEntity::toDomain);
    }

    @Override
    public Optional<CartItem> findByUserIdAndDishId(Integer userId, Integer dishId) {
        return cartItemJpaRepository.findByUserIdAndDishId(userId, dishId).map(CartItemJpaEntity::toDomain);
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return cartItemJpaRepository.save(CartItemJpaEntity.fromDomain(cartItem)).toDomain();
    }

    @Override
    public void deleteById(Integer cartItemId) {
        cartItemJpaRepository.deleteById(cartItemId);
    }

    @Override
    public void deleteByUserId(Integer userId) {
        cartItemJpaRepository.deleteByUserId(userId);
    }
}
