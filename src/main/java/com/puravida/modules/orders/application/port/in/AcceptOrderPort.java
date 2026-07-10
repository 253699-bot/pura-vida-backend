package com.puravida.modules.orders.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.orders.application.dto.OrderResponse;

public interface AcceptOrderPort {

    OrderResponse accept(Integer orderId, AuthenticatedUser authenticatedUser);
}
