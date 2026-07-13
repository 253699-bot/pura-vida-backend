package com.puravida.modules.dashboard.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.dashboard.application.dto.TopDishesResponse;

public interface GetTopDishesPort {

    TopDishesResponse getTopDishes(String from, String to, AuthenticatedUser authenticatedUser);
}
