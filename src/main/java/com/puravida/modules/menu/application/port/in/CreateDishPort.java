package com.puravida.modules.menu.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.CreateDishRequest;
import com.puravida.modules.menu.application.dto.DishResponse;

public interface CreateDishPort {

    DishResponse create(CreateDishRequest request, AuthenticatedUser authenticatedUser);
}
