package com.puravida.modules.cart.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.cart.application.dto.CartResponse;
import com.puravida.modules.cart.application.port.in.GetCartPort;
import com.puravida.modules.cart.application.port.out.CartDishRepositoryPort;
import com.puravida.modules.cart.application.port.out.CartRepositoryPort;
import com.puravida.modules.cart.domain.model.CartItem;
import com.puravida.modules.users.domain.model.User;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCartUseCase implements GetCartPort {

    private final CartRepositoryPort cartRepositoryPort;
    private final CartDishRepositoryPort cartDishRepositoryPort;
    private final CartAuthorizationService authorizationService;
    private final CartResponseAssembler responseAssembler;

    public GetCartUseCase(
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
    @Transactional(readOnly = true)
    public CartResponse getCart(AuthenticatedUser authenticatedUser) {
        User user = authorizationService.requireActiveUser(authenticatedUser);
        List<CartItem> items = cartRepositoryPort.findByUserId(user.id());
        return responseAssembler.cart(
                items,
                cartDishRepositoryPort.findAllByIds(items.stream().map(CartItem::dishId).toList())
        );
    }
}
