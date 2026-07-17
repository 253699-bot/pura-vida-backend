package com.puravida.modules.cart.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.orders.application.dto.OrderResponse;

public interface CheckoutCartPort {

    OrderResponse checkout(AuthenticatedUser authenticatedUser);
}
