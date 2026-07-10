package com.puravida.modules.menu.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.menu.application.dto.TodayMenuItemResponse;
import com.puravida.modules.menu.application.dto.TodayMenuResponse;
import com.puravida.modules.menu.application.dto.UpdateMenuItemAvailabilityRequest;
import com.puravida.modules.menu.application.dto.UpdateTodayMenuItemRequest;
import com.puravida.modules.menu.application.dto.UpdateTodayMenuRequest;
import com.puravida.modules.menu.application.port.in.GetTodayMenuPort;
import com.puravida.modules.menu.application.port.in.UpdateTodayMenuItemAvailabilityPort;
import com.puravida.modules.menu.application.port.in.UpdateTodayMenuPort;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = MenuController.class, properties = "debug=false")
@Import(GlobalExceptionHandler.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetTodayMenuPort getTodayMenuPort;

    @MockitoBean
    private UpdateTodayMenuPort updateTodayMenuPort;

    @MockitoBean
    private UpdateTodayMenuItemAvailabilityPort updateTodayMenuItemAvailabilityPort;

    @MockitoBean
    private AuthenticateBearerTokenPort authenticateBearerTokenPort;

    @Test
    void getTodayIsPublicAndReturnsControlledResponse() throws Exception {
        when(getTodayMenuPort.getToday())
                .thenReturn(TodayMenuResponse.notConfigured(LocalDate.of(2026, 7, 10)));

        mockMvc.perform(get("/api/v1/menu/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.configured", is(false)))
                .andExpect(jsonPath("$.data.fecha", is("2026-07-10")))
                .andExpect(jsonPath("$.data.items").isArray());

        verifyNoInteractions(authenticateBearerTokenPort);
    }

    @Test
    void putTodayRequiresBearerTokenAndUpdatesMenu() throws Exception {
        AuthenticatedUser authenticatedUser = authenticatedEncargada();
        TodayMenuResponse response = TodayMenuResponse.configured(
                LocalDate.of(2026, 7, 10),
                List.of()
        );
        when(authenticateBearerTokenPort.authenticate("Bearer test-token")).thenReturn(authenticatedUser);
        when(updateTodayMenuPort.updateToday(any(UpdateTodayMenuRequest.class), eq(authenticatedUser)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/menu/today")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateTodayMenuRequest(List.of(new UpdateTodayMenuItemRequest(10)))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.configured", is(true)));
    }

    @Test
    void putTodayReturnsUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(put("/api/v1/menu/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateTodayMenuRequest(List.of(new UpdateTodayMenuItemRequest(10)))
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void putTodayReturnsForbiddenForClientRole() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(8, "cliente@example.com", UserRole.CLIENTE);
        when(authenticateBearerTokenPort.authenticate("Bearer test-token")).thenReturn(authenticatedUser);
        when(updateTodayMenuPort.updateToday(any(UpdateTodayMenuRequest.class), eq(authenticatedUser)))
                .thenThrow(new ForbiddenException("No tienes permisos para administrar el menu."));

        mockMvc.perform(put("/api/v1/menu/today")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateTodayMenuRequest(List.of(new UpdateTodayMenuItemRequest(10)))
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void patchAvailabilityRequiresBearerTokenAndUpdatesItem() throws Exception {
        AuthenticatedUser authenticatedUser = authenticatedEncargada();
        TodayMenuItemResponse response = menuItemResponse(false);
        when(authenticateBearerTokenPort.authenticate("Bearer test-token")).thenReturn(authenticatedUser);
        when(updateTodayMenuItemAvailabilityPort.updateAvailability(
                eq(5),
                any(UpdateMenuItemAvailabilityRequest.class),
                eq(authenticatedUser)
        )).thenReturn(response);

        mockMvc.perform(patch("/api/v1/menu/today/items/5/availability")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateMenuItemAvailabilityRequest(false)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.id", is(5)))
                .andExpect(jsonPath("$.data.disponible", is(false)));
    }

    @Test
    void patchAvailabilityReturnsUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(patch("/api/v1/menu/today/items/5/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateMenuItemAvailabilityRequest(false)
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void patchAvailabilityReturnsForbiddenForClientRole() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(8, "cliente@example.com", UserRole.CLIENTE);
        when(authenticateBearerTokenPort.authenticate("Bearer test-token")).thenReturn(authenticatedUser);
        when(updateTodayMenuItemAvailabilityPort.updateAvailability(
                eq(5),
                any(UpdateMenuItemAvailabilityRequest.class),
                eq(authenticatedUser)
        )).thenThrow(new ForbiddenException("No tienes permisos para administrar el menu."));

        mockMvc.perform(patch("/api/v1/menu/today/items/5/availability")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateMenuItemAvailabilityRequest(false)
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    private AuthenticatedUser authenticatedEncargada() {
        return new AuthenticatedUser(2, "encargada@example.com", UserRole.ENCARGADA);
    }

    private TodayMenuItemResponse menuItemResponse(boolean disponible) {
        return new TodayMenuItemResponse(
                5,
                10,
                "Tacos",
                "Orden de tacos",
                "platillo_fuerte",
                new BigDecimal("65.00"),
                disponible
        );
    }
}
