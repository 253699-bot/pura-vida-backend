package com.puravida.modules.menu.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.menu.application.dto.CreateDishRequest;
import com.puravida.modules.menu.application.dto.DishResponse;
import com.puravida.modules.menu.application.port.in.CreateDishPort;
import com.puravida.modules.menu.application.port.in.GetActiveDishesPort;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import com.puravida.shared.web.GlobalExceptionHandler;
import java.math.BigDecimal;
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

@WebMvcTest(value = AdminDishController.class, properties = "debug=false")
@Import(GlobalExceptionHandler.class)
class AdminDishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateDishPort createDishPort;

    @MockitoBean
    private GetActiveDishesPort getActiveDishesPort;

    @MockitoBean
    private AuthenticateBearerTokenPort authenticateBearerTokenPort;

    @Test
    void createsDishForEncargada() throws Exception {
        AuthenticatedUser encargada = encargada();
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(createDishPort.create(any(CreateDishRequest.class), eq(encargada))).thenReturn(dishResponse());

        mockMvc.perform(post("/api/v1/admin/dishes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.id", is(12)))
                .andExpect(jsonPath("$.data.activo", is(true)))
                .andExpect(jsonPath("$.data.precioBase", is(85.00)));
    }

    @Test
    void createReturnsUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(post("/api/v1/admin/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void createReturnsForbiddenForClientRole() throws Exception {
        AuthenticatedUser cliente = new AuthenticatedUser(8, "cliente@example.com", UserRole.CLIENTE);
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(cliente);
        when(createDishPort.create(any(CreateDishRequest.class), eq(cliente)))
                .thenThrow(new ForbiddenException("No tienes permisos para administrar el menu."));

        mockMvc.perform(post("/api/v1/admin/dishes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void createValidatesInvalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/admin/dishes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"tipoPlatillo\":\"invalido\",\"precioBase\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void listsActiveDishesForEncargada() throws Exception {
        AuthenticatedUser encargada = encargada();
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(getActiveDishesPort.getActive(encargada)).thenReturn(List.of(dishResponse()));

        mockMvc.perform(get("/api/v1/admin/dishes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data[0].id", is(12)))
                .andExpect(jsonPath("$.data[0].activo", is(true)));
    }

    private AuthenticatedUser encargada() {
        return new AuthenticatedUser(2, "encargada@example.com", UserRole.ENCARGADA);
    }

    private CreateDishRequest createRequest() {
        return new CreateDishRequest(
                "Comida corrida",
                "Incluye sopa y guisado.",
                "platillo_fuerte",
                new BigDecimal("85.00")
        );
    }

    private DishResponse dishResponse() {
        return new DishResponse(
                12,
                "Comida corrida",
                "Incluye sopa y guisado.",
                "platillo_fuerte",
                new BigDecimal("85.00"),
                true,
                LocalDateTime.of(2026, 7, 12, 10, 0),
                null
        );
    }
}
