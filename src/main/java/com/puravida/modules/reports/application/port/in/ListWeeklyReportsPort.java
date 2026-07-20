package com.puravida.modules.reports.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.reports.application.dto.WeeklyReportListItemResponse;
import java.util.List;

public interface ListWeeklyReportsPort {
    List<WeeklyReportListItemResponse> list(AuthenticatedUser authenticatedUser);
}
