package com.puravida.modules.cart.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.cart.application.dto.CartResponse;

public interface GetCartPort {

    CartResponse getCart(AuthenticatedUser authenticatedUser);
}
