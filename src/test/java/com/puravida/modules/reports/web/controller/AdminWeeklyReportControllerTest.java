package com.puravida.modules.reports.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.reports.application.dto.WeeklyReportOrdersSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportPdf;
import com.puravida.modules.reports.application.dto.WeeklyReportSalesSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSourceSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import com.puravida.modules.reports.application.port.in.GenerateWeeklyReportPdfPort;
import com.puravida.modules.reports.application.port.in.GetWeeklyReportSummaryPort;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import com.puravida.shared.web.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AdminWeeklyReportController.class, properties = "debug=false")
@Import(GlobalExceptionHandler.class)
class AdminWeeklyReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetWeeklyReportSummaryPort getWeeklyReportSummaryPort;

    @MockitoBean
    private GenerateWeeklyReportPdfPort generateWeeklyReportPdfPort;

    @MockitoBean
    private AuthenticateBearerTokenPort authenticateBearerTokenPort;

    @Test
    void returnsWeeklySummaryAndPdfForEncargada() throws Exception {
        AuthenticatedUser encargada = encargada();
        WeeklyReportSummary summary = summary();
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(getWeeklyReportSummaryPort.getSummary("2026-07-06", encargada)).thenReturn(summary);
        when(generateWeeklyReportPdfPort.generate("2026-07-06", encargada))
                .thenReturn(new WeeklyReportPdf("%PDF-test".getBytes(), LocalDate.of(2026, 7, 6)));

        mockMvc.perform(get("/api/v1/admin/reports/weekly/summary")
                        .queryParam("weekStart", "2026-07-06")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.ventas.totalActivo", is(300.00)));

        mockMvc.perform(get("/api/v1/admin/reports/weekly/pdf")
                        .queryParam("weekStart", "2026-07-06")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("reporte-semanal-puravida-2026-07-06.pdf")));
    }

    @Test
    void returnsUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(get("/api/v1/admin/reports/weekly/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void returnsForbiddenForClientRole() throws Exception {
        AuthenticatedUser client = new AuthenticatedUser(1, "cliente@example.com", UserRole.CLIENTE);
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(client);
        when(generateWeeklyReportPdfPort.generate(null, client))
                .thenThrow(new ForbiddenException("No tienes permisos para consultar el dashboard."));

        mockMvc.perform(get("/api/v1/admin/reports/weekly/pdf")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    private WeeklyReportSummary summary() {
        return new WeeklyReportSummary(
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 12),
                LocalDateTime.of(2026, 7, 12, 18, 0),
                new WeeklyReportSalesSummary(
                        new BigDecimal("300.00"),
                        new BigDecimal("50.00"),
                        3,
                        1,
                        new WeeklyReportSourceSummary(1, new BigDecimal("100.00")),
                        new WeeklyReportSourceSummary(2, new BigDecimal("200.00"))
                ),
                new WeeklyReportOrdersSummary(2, 3, 1),
                List.of()
        );
    }

    private AuthenticatedUser encargada() {
        return new AuthenticatedUser(4, "encargada@example.com", UserRole.ENCARGADA);
    }
}
