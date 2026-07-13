package com.puravida.modules.menu.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.DishResponse;
import java.util.List;

public interface GetActiveDishesPort {

    List<DishResponse> getActive(AuthenticatedUser authenticatedUser);
}
