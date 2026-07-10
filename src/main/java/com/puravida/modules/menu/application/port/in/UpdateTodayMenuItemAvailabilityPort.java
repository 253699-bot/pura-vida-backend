package com.puravida.modules.menu.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.TodayMenuItemResponse;
import com.puravida.modules.menu.application.dto.UpdateMenuItemAvailabilityRequest;

public interface UpdateTodayMenuItemAvailabilityPort {

    TodayMenuItemResponse updateAvailability(
            Integer menuItemId,
            UpdateMenuItemAvailabilityRequest request,
            AuthenticatedUser authenticatedUser
    );
}
