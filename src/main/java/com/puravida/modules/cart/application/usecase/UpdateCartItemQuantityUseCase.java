package com.puravida.modules.cart.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.cart.application.dto.CartItemResponse;
import com.puravida.modules.cart.application.dto.UpdateCartItemQuantityRequest;
import com.puravida.modules.cart.application.port.in.UpdateCartItemQuantityPort;
import com.puravida.modules.cart.application.port.out.CartDishRepositoryPort;
import com.puravida.modules.cart.application.port.out.CartRepositoryPort;
import com.puravida.modules.cart.domain.exception.CartValidationException;
import com.puravida.modules.cart.domain.model.CartDish;
import com.puravida.modules.cart.domain.model.CartItem;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCartItemQuantityUseCase implements UpdateCartItemQuantityPort {

    private final CartRepositoryPort cartRepositoryPort;
    private final CartDishRepositoryPort cartDishRepositoryPort;
    private final CartAuthorizationService authorizationService;
    private final CartResponseAssembler responseAssembler;

    public UpdateCartItemQuantityUseCase(
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
    public CartItemResponse updateQuantity(
            Integer cartItemId,
            UpdateCartItemQuantityRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        if (request == null || request.cantidad() == null || request.cantidad() <= 0) {
            throw new CartValidationException("La cantidad debe ser mayor a cero.");
        }
        User user = authorizationService.requireActiveUser(authenticatedUser);
        CartItem item = cartRepositoryPort.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Item de carrito no encontrado."));
        verifyOwnership(item, user);
        CartDish dish = cartDishRepositoryPort.findById(item.dishId())
                .orElseThrow(() -> new NotFoundException("Platillo no encontrado."));
        CartItem savedItem = cartRepositoryPort.save(item.withCantidad(request.cantidad()));
        return responseAssembler.item(savedItem, dish);
    }

    private void verifyOwnership(CartItem item, User user) {
        if (!item.userId().equals(user.id())) {
            throw new ForbiddenException("No tienes permisos para modificar este item de carrito.");
        }
    }
}
