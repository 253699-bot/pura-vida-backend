package com.puravida.modules.reports.web.controller;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.reports.application.dto.CreateWeeklyReportRequest;
import com.puravida.modules.reports.application.dto.WeeklyReportListItemResponse;
import com.puravida.modules.reports.application.dto.WeeklyReportPdf;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import com.puravida.modules.reports.application.port.in.CreateWeeklyReportPort;
import com.puravida.modules.reports.application.port.in.GenerateWeeklyReportPdfPort;
import com.puravida.modules.reports.application.port.in.GenerateStoredWeeklyReportPdfPort;
import com.puravida.modules.reports.application.port.in.GetWeeklyReportSummaryPort;
import com.puravida.modules.reports.application.port.in.ListWeeklyReportsPort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/admin/reports/weekly")
public class AdminWeeklyReportController {

    private final GetWeeklyReportSummaryPort getWeeklyReportSummaryPort;
    private final GenerateWeeklyReportPdfPort generateWeeklyReportPdfPort;
    private final ListWeeklyReportsPort listWeeklyReportsPort;
    private final CreateWeeklyReportPort createWeeklyReportPort;
    private final GenerateStoredWeeklyReportPdfPort generateStoredWeeklyReportPdfPort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public AdminWeeklyReportController(
            GetWeeklyReportSummaryPort getWeeklyReportSummaryPort,
            GenerateWeeklyReportPdfPort generateWeeklyReportPdfPort,
            ListWeeklyReportsPort listWeeklyReportsPort,
            CreateWeeklyReportPort createWeeklyReportPort,
            GenerateStoredWeeklyReportPdfPort generateStoredWeeklyReportPdfPort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.getWeeklyReportSummaryPort = getWeeklyReportSummaryPort;
        this.generateWeeklyReportPdfPort = generateWeeklyReportPdfPort;
        this.listWeeklyReportsPort = listWeeklyReportsPort;
        this.createWeeklyReportPort = createWeeklyReportPort;
        this.generateStoredWeeklyReportPdfPort = generateStoredWeeklyReportPdfPort;
        this.authenticateBearerTokenPort = authenticateBearerTokenPort;
    }

    @GetMapping
    public ApiResponse<List<WeeklyReportListItemResponse>> list(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(listWeeklyReportsPort.list(authenticatedUser));
    }

    @PostMapping
    public ApiResponse<WeeklyReportListItemResponse> create(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody CreateWeeklyReportRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(createWeeklyReportPort.create(request, authenticatedUser));
    }

    @GetMapping("/summary")
    public ApiResponse<WeeklyReportSummary> getSummary(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestParam(value = "weekStart", required = false) String weekStart
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getWeeklyReportSummaryPort.getSummary(weekStart, authenticatedUser));
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generatePdf(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestParam(value = "weekStart", required = false) String weekStart
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        WeeklyReportPdf report = generateWeeklyReportPdfPort.generate(weekStart, authenticatedUser);
        String fileName = "reporte-semanal-puravida-" + report.weekStart() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(report.content().length)
                .body(report.content());
    }

    @GetMapping(value = "/{reportId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateStoredPdf(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable Integer reportId
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        WeeklyReportPdf report = generateStoredWeeklyReportPdfPort.generate(reportId, authenticatedUser);
        String fileName = "reporte-semanal-puravida-" + report.weekStart() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="
                        + Character.toString(34) + fileName + Character.toString(34))
                .contentLength(report.content().length)
                .body(report.content());
    }
}
