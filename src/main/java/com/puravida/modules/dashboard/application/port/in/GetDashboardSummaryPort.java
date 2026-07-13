package com.puravida.modules.dashboard.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.dashboard.application.dto.DashboardSummaryResponse;

public interface GetDashboardSummaryPort {

    DashboardSummaryResponse getSummary(String from, String to, AuthenticatedUser authenticatedUser);
}
