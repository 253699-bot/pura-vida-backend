package com.puravida.modules.reports.application.port.out;

import com.puravida.modules.reports.application.dto.WeeklyReportSummary;

public interface WeeklyReportPdfGeneratorPort {

    byte[] generate(WeeklyReportSummary report);
}
