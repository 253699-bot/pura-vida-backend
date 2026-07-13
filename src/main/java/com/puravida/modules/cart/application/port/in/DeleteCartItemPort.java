package com.puravida.modules.cart.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;

public interface DeleteCartItemPort {

    void delete(Integer cartItemId, AuthenticatedUser authenticatedUser);
}
