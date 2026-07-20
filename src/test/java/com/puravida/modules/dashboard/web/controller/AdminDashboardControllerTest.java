package com.puravida.modules.dashboard.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.dashboard.application.dto.DashboardDailySalesResponse;
import com.puravida.modules.dashboard.application.dto.DashboardHourlySalesResponse;
import com.puravida.modules.dashboard.application.dto.DashboardOperationResponse;
import com.puravida.modules.dashboard.application.dto.DashboardOrdersResponse;
import com.puravida.modules.dashboard.application.dto.DashboardSalesResponse;
import com.puravida.modules.dashboard.application.dto.DashboardSummaryResponse;
import com.puravida.modules.dashboard.application.dto.SourceSalesSummaryResponse;
import com.puravida.modules.dashboard.application.dto.TodayDashboardResponse;
import com.puravida.modules.dashboard.application.dto.TopDishResponse;
import com.puravida.modules.dashboard.application.dto.TopDishesResponse;
import com.puravida.modules.dashboard.application.port.in.GetDashboardSummaryPort;
import com.puravida.modules.dashboard.application.port.in.GetTodayDashboardPort;
import com.puravida.modules.dashboard.application.port.in.GetTopDishesPort;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import com.puravida.shared.web.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AdminDashboardController.class, properties = "debug=false")
@Import(GlobalExceptionHandler.class)
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetTodayDashboardPort getTodayDashboardPort;

    @MockitoBean
    private GetDashboardSummaryPort getDashboardSummaryPort;

    @MockitoBean
    private GetTopDishesPort getTopDishesPort;

    @MockitoBean
    private AuthenticateBearerTokenPort authenticateBearerTokenPort;

    @Test
    void returnsTodayDashboardForEncargada() throws Exception {
        AuthenticatedUser encargada = encargada();
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(getTodayDashboardPort.getToday(encargada)).thenReturn(todayResponse());

        mockMvc.perform(get("/api/v1/admin/dashboard/today")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.ventas.totalActivo", is(300.00)))
                .andExpect(jsonPath("$.data.ventas.cantidadAnuladas", is(1)))
                .andExpect(jsonPath("$.data.pedidos.cancelados", is(4)))
                .andExpect(jsonPath("$.data.ventasPorHora[0].hora", is(12)))
                .andExpect(jsonPath("$.data.operacion.negocioAbierto", is(true)));
    }

    @Test
    void returnsSummaryAndTopDishes() throws Exception {
        AuthenticatedUser encargada = encargada();
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 7, 11);
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(getDashboardSummaryPort.getSummary("2026-07-01", "2026-07-11", encargada))
                .thenReturn(new DashboardSummaryResponse(
                        from,
                        to,
                        sales(),
                        new DashboardOrdersResponse(1, 2, 3, 4),
                        List.of(new DashboardDailySalesResponse(from, new BigDecimal("75.00"), 1))
                ));
        when(getTopDishesPort.getTopDishes("2026-07-01", "2026-07-11", encargada))
                .thenReturn(new TopDishesResponse(
                        from,
                        to,
                        List.of(new TopDishResponse(8, "Cochito", 12, new BigDecimal("960.00")))
                ));

        mockMvc.perform(get("/api/v1/admin/dashboard/summary")
                        .queryParam("from", "2026-07-01")
                        .queryParam("to", "2026-07-11")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pedidos.rechazados", is(3)))
                .andExpect(jsonPath("$.data.pedidos.cancelados", is(4)))
                .andExpect(jsonPath("$.data.ventasPorDia[0].fecha", is("2026-07-01")));

        mockMvc.perform(get("/api/v1/admin/dashboard/top-dishes")
                        .queryParam("from", "2026-07-01")
                        .queryParam("to", "2026-07-11")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].nombre", is("Cochito")));
    }

    @Test
    void returnsUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(get("/api/v1/admin/dashboard/today"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void returnsForbiddenForClientRole() throws Exception {
        AuthenticatedUser client = new AuthenticatedUser(1, "cliente@example.com", UserRole.CLIENTE);
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(client);
        when(getTodayDashboardPort.getToday(client))
                .thenThrow(new ForbiddenException("No tienes permisos para consultar el dashboard."));

        mockMvc.perform(get("/api/v1/admin/dashboard/today")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    private TodayDashboardResponse todayResponse() {
        return new TodayDashboardResponse(
                LocalDate.of(2026, 7, 11),
                sales(),
                new DashboardOrdersResponse(2, 3, 1, 4),
                new DashboardOperationResponse(true, true, null),
                List.of(new DashboardHourlySalesResponse(12, new BigDecimal("300.00"), 3))
        );
    }

    private DashboardSalesResponse sales() {
        return new DashboardSalesResponse(
                new BigDecimal("300.00"),
                new BigDecimal("50.00"),
                3,
                1,
                new SourceSalesSummaryResponse(1, new BigDecimal("100.00")),
                new SourceSalesSummaryResponse(2, new BigDecimal("200.00"))
        );
    }

    private AuthenticatedUser encargada() {
        return new AuthenticatedUser(4, "encargada@example.com", UserRole.ENCARGADA);
    }
}
