package com.puravida.modules.cart.application.usecase;

import com.puravida.modules.cart.application.dto.CartItemResponse;
import com.puravida.modules.cart.application.dto.CartResponse;
import com.puravida.modules.cart.domain.model.CartDish;
import com.puravida.modules.cart.domain.model.CartItem;
import com.puravida.shared.domain.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class CartResponseAssembler {

    public CartResponse cart(List<CartItem> items, List<CartDish> dishes) {
        Map<Integer, CartDish> dishesById = dishes.stream()
                .collect(java.util.stream.Collectors.toMap(CartDish::id, Function.identity()));
        List<CartItemResponse> responses = items.stream()
                .map(item -> item(item, requiredDish(dishesById, item.dishId())))
                .toList();
        BigDecimal total = responses.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(responses, total);
    }

    public CartItemResponse item(CartItem item, CartDish dish) {
        BigDecimal subtotal = item.precioUnitario().multiply(BigDecimal.valueOf(item.cantidad()));
        return new CartItemResponse(
                item.id(),
                item.dishId(),
                dish.nombre(),
                item.cantidad(),
                item.precioUnitario(),
                subtotal
        );
    }

    private CartDish requiredDish(Map<Integer, CartDish> dishesById, Integer dishId) {
        CartDish dish = dishesById.get(dishId);
        if (dish == null) {
            throw new NotFoundException("Platillo no encontrado.");
        }
        return dish;
    }
}
