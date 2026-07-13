package com.puravida.modules.dashboard.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.dashboard.application.dto.TodayDashboardResponse;

public interface GetTodayDashboardPort {

    TodayDashboardResponse getToday(AuthenticatedUser authenticatedUser);
}
