package com.puravida.modules.cart.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.cart.application.dto.AddCartItemRequest;
import com.puravida.modules.cart.application.dto.CartItemResponse;
import com.puravida.modules.cart.application.port.in.AddCartItemPort;
import com.puravida.modules.cart.application.port.out.CartDishRepositoryPort;
import com.puravida.modules.cart.application.port.out.CartRepositoryPort;
import com.puravida.modules.cart.domain.exception.CartValidationException;
import com.puravida.modules.cart.domain.model.CartDish;
import com.puravida.modules.cart.domain.model.CartItem;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddCartItemUseCase implements AddCartItemPort {

    private final CartRepositoryPort cartRepositoryPort;
    private final CartDishRepositoryPort cartDishRepositoryPort;
    private final CartAuthorizationService authorizationService;
    private final CartResponseAssembler responseAssembler;

    public AddCartItemUseCase(
            CartRepositoryPort cartRepositoryPort,
            CartDishRepositoryPort cartDishRepositoryPort,
            CartAuthorizationService authorizationService,
            CartResponseAssembler responseAssembler
    ) {
        this.cartRepositoryPort = cartRepositoryPort;
        this.cartDishRepositoryPort = cartDishRepositoryPort;
        this.authorizationService = authorizationService;
        this.responseAssembler = responseAssembler;
    }

    @Override
    @Transactional
    public CartItemResponse add(AddCartItemRequest request, AuthenticatedUser authenticatedUser) {
        validate(request);
        User user = authorizationService.requireActiveUser(authenticatedUser);
        CartDish dish = cartDishRepositoryPort.findById(request.dishId())
                .orElseThrow(() -> new NotFoundException("Platillo no encontrado."));
        if (!dish.activo()) {
            throw new ConflictException("El platillo no esta activo.");
        }

        CartItem item = cartRepositoryPort.findByUserIdAndDishId(user.id(), dish.id())
                .map(existing -> existing.withCantidad(existing.cantidad() + request.cantidad()))
                .orElseGet(() -> CartItem.create(user.id(), dish.id(), request.cantidad(), dish.precioBase()));
        CartItem savedItem = cartRepositoryPort.save(item);
        return responseAssembler.item(savedItem, dish);
    }

    private void validate(AddCartItemRequest request) {
        if (request == null || request.dishId() == null || request.cantidad() == null || request.cantidad() <= 0) {
            throw new CartValidationException("El platillo y una cantidad positiva son obligatorios.");
        }
    }
}
