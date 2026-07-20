package com.puravida.modules.reports.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.reports.application.dto.CreateWeeklyReportRequest;
import com.puravida.modules.reports.application.dto.WeeklyReportListItemResponse;

public interface CreateWeeklyReportPort {
    WeeklyReportListItemResponse create(
            CreateWeeklyReportRequest request,
            AuthenticatedUser authenticatedUser
    );
}
