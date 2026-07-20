package com.puravida.modules.menu.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.DishResponse;

public interface UpdateDishImagePort {
    DishResponse update(Integer dishId, byte[] content, String mediaType, AuthenticatedUser authenticatedUser);
}