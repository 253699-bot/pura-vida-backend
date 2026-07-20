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
import com.puravida.modules.orders.domain.model.OrderableMenuItem;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCartItemQuantityUseCase implements UpdateCartItemQuantityPort {

    private final CartRepositoryPort cartRepositoryPort;
    private final CartDishRepositoryPort cartDishRepositoryPort;
    private final CartAuthorizationService authorizationService;
    private final CartResponseAssembler responseAssembler;
    private final CartSellabilityService sellabilityService;

    public UpdateCartItemQuantityUseCase(
            CartRepositoryPort cartRepositoryPort,
            CartDishRepositoryPort cartDishRepositoryPort,
            CartAuthorizationService authorizationService,
            CartResponseAssembler responseAssembler,
            CartSellabilityService sellabilityService
    ) {
        this.cartRepositoryPort = cartRepositoryPort;
        this.cartDishRepositoryPort = cartDishRepositoryPort;
        this.authorizationService = authorizationService;
        this.responseAssembler = responseAssembler;
        this.sellabilityService = sellabilityService;
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
        User user = authorizationService.requireClient(authenticatedUser);
        CartItem item = cartRepositoryPort.findByIdAndUserId(cartItemId, user.id())
                .orElseThrow(() -> new NotFoundException("Item de carrito no encontrado."));
        OrderableMenuItem menuItem = sellabilityService.requireSellableDish(item.dishId());
        CartDish dish = cartDishRepositoryPort.findById(item.dishId())
                .orElseThrow(() -> new NotFoundException("Platillo no encontrado."));
        CartItem savedItem = cartRepositoryPort.save(
                item.withCantidadAndPrecio(request.cantidad(), menuItem.precioDia())
        );
        return responseAssembler.item(savedItem, dish);
    }
}
