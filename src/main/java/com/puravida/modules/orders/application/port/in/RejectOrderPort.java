package com.puravida.modules.orders.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.dto.RejectOrderRequest;

public interface RejectOrderPort {

    OrderResponse reject(Integer orderId, RejectOrderRequest request, AuthenticatedUser authenticatedUser);
}
