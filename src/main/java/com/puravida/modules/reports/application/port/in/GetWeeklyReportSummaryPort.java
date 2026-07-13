package com.puravida.modules.reports.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;

public interface GetWeeklyReportSummaryPort {

    WeeklyReportSummary getSummary(String weekStart, AuthenticatedUser authenticatedUser);
}
