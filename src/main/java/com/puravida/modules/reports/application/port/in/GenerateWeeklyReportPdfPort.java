package com.puravida.modules.reports.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.reports.application.dto.WeeklyReportPdf;

public interface GenerateWeeklyReportPdfPort {

    WeeklyReportPdf generate(String weekStart, AuthenticatedUser authenticatedUser);
}
