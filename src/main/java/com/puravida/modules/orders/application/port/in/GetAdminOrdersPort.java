package com.puravida.modules.orders.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.orders.application.dto.OrderSummaryResponse;
import java.util.List;

public interface GetAdminOrdersPort {

    List<OrderSummaryResponse> getOrders(String estado, AuthenticatedUser authenticatedUser);
}
