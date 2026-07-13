package com.puravida.modules.cart.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.cart.application.dto.AddCartItemRequest;
import com.puravida.modules.cart.application.dto.CartItemResponse;

public interface AddCartItemPort {

    CartItemResponse add(AddCartItemRequest request, AuthenticatedUser authenticatedUser);
}
