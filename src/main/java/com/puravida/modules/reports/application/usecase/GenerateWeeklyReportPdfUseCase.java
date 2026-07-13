package com.puravida.modules.reports.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.reports.application.dto.WeeklyReportPdf;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import com.puravida.modules.reports.application.port.in.GenerateWeeklyReportPdfPort;
import com.puravida.modules.reports.application.port.in.GetWeeklyReportSummaryPort;
import com.puravida.modules.reports.application.port.out.WeeklyReportPdfGeneratorPort;
import org.springframework.stereotype.Service;

@Service
public class GenerateWeeklyReportPdfUseCase implements GenerateWeeklyReportPdfPort {

    private final GetWeeklyReportSummaryPort getWeeklyReportSummaryPort;
    private final WeeklyReportPdfGeneratorPort pdfGeneratorPort;

    public GenerateWeeklyReportPdfUseCase(
            GetWeeklyReportSummaryPort getWeeklyReportSummaryPort,
            WeeklyReportPdfGeneratorPort pdfGeneratorPort
    ) {
        this.getWeeklyReportSummaryPort = getWeeklyReportSummaryPort;
        this.pdfGeneratorPort = pdfGeneratorPort;
    }

    @Override
    public WeeklyReportPdf generate(String weekStart, AuthenticatedUser authenticatedUser) {
        WeeklyReportSummary report = getWeeklyReportSummaryPort.getSummary(weekStart, authenticatedUser);
        return new WeeklyReportPdf(pdfGeneratorPort.generate(report), report.semanaInicio());
    }
}
