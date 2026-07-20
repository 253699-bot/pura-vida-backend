package com.puravida.modules.cart.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.cart.application.port.in.DeleteCartItemPort;
import com.puravida.modules.cart.application.port.out.CartRepositoryPort;
import com.puravida.modules.cart.domain.model.CartItem;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteCartItemUseCase implements DeleteCartItemPort {

    private final CartRepositoryPort cartRepositoryPort;
    private final CartAuthorizationService authorizationService;

    public DeleteCartItemUseCase(CartRepositoryPort cartRepositoryPort, CartAuthorizationService authorizationService) {
        this.cartRepositoryPort = cartRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public void delete(Integer cartItemId, AuthenticatedUser authenticatedUser) {
        User user = authorizationService.requireClient(authenticatedUser);
        CartItem item = cartRepositoryPort.findByIdAndUserId(cartItemId, user.id())
                .orElseThrow(() -> new NotFoundException("Item de carrito no encontrado."));
        cartRepositoryPort.deleteById(item.id());
    }
}
