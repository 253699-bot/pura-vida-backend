package com.puravida.modules.cart.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.cart.application.dto.CartItemResponse;
import com.puravida.modules.cart.application.dto.UpdateCartItemQuantityRequest;

public interface UpdateCartItemQuantityPort {

    CartItemResponse updateQuantity(Integer cartItemId, UpdateCartItemQuantityRequest request,
                                    AuthenticatedUser authenticatedUser);
}
