package com.puravida.modules.menu.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.TodayMenuResponse;
import com.puravida.modules.menu.application.dto.UpdateTodayMenuRequest;

public interface UpdateTodayMenuPort {

    TodayMenuResponse updateToday(UpdateTodayMenuRequest request, AuthenticatedUser authenticatedUser);
}
