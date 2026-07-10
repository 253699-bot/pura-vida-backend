package com.puravida.modules.business.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.business.application.dto.TodayBusinessStatusResponse;
import com.puravida.modules.business.application.dto.UpdateTodayBusinessStatusRequest;
import com.puravida.modules.business.application.port.in.GetTodayBusinessStatusPort;
import com.puravida.modules.business.application.port.in.UpdateTodayBusinessStatusPort;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.UnauthorizedException;
import com.puravida.shared.web.GlobalExceptionHandler;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = BusinessStatusController.class, properties = "debug=false")
@Import(GlobalExceptionHandler.class)
class BusinessStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetTodayBusinessStatusPort getTodayBusinessStatusPort;

    @MockitoBean
    private UpdateTodayBusinessStatusPort updateTodayBusinessStatusPort;

    @MockitoBean
    private AuthenticateBearerTokenPort authenticateBearerTokenPort;

    @Test
    void getTodayIsPublicAndReturnsControlledResponse() throws Exception {
        when(getTodayBusinessStatusPort.getToday())
                .thenReturn(TodayBusinessStatusResponse.notConfigured(LocalDate.of(2026, 7, 10)));

        mockMvc.perform(get("/api/v1/business/status/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.configured", is(false)))
                .andExpect(jsonPath("$.data.fecha", is("2026-07-10")));

        verifyNoInteractions(authenticateBearerTokenPort);
    }

    @Test
    void putTodayRequiresBearerTokenAndUpdatesStatus() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                2,
                "encargada@example.com",
                UserRole.ENCARGADA
        );
        TodayBusinessStatusResponse response = new TodayBusinessStatusResponse(
                true,
                4,
                LocalDate.of(2026, 7, 10),
                false,
                "Cierre por mantenimiento",
                2,
                LocalDateTime.of(2026, 7, 10, 8, 0),
                LocalDateTime.of(2026, 7, 10, 9, 0)
        );
        when(authenticateBearerTokenPort.authenticate("Bearer test-token")).thenReturn(authenticatedUser);
        when(updateTodayBusinessStatusPort.updateToday(any(UpdateTodayBusinessStatusRequest.class), eq(authenticatedUser)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/business/status/today")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateTodayBusinessStatusRequest(false, "Cierre por mantenimiento")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.configured", is(true)))
                .andExpect(jsonPath("$.data.abierto", is(false)))
                .andExpect(jsonPath("$.data.motivoCierre", is("Cierre por mantenimiento")))
                .andExpect(jsonPath("$.data.registradoPor", is(2)));
    }

    @Test
    void putTodayReturnsUnauthorizedWhenBearerTokenIsMissing() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(put("/api/v1/business/status/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateTodayBusinessStatusRequest(true, null)
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }
}
