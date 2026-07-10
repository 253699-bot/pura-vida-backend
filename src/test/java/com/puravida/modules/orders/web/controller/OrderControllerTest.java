package com.puravida.modules.orders.web.controller;

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
import com.puravida.modules.orders.application.dto.CreateOrderItemRequest;
import com.puravida.modules.orders.application.dto.CreateOrderRequest;
import com.puravida.modules.orders.application.dto.OrderItemResponse;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.dto.OrderSummaryResponse;
import com.puravida.modules.orders.application.port.in.CreateOrderPort;
import com.puravida.modules.orders.application.port.in.GetMyOrdersPort;
import com.puravida.modules.orders.application.port.in.GetOrderDetailPort;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import com.puravida.shared.web.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = OrderController.class, properties = "debug=false")
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateOrderPort createOrderPort;

    @MockitoBean
    private GetMyOrdersPort getMyOrdersPort;

    @MockitoBean
    private GetOrderDetailPort getOrderDetailPort;

    @MockitoBean
    private AuthenticateBearerTokenPort authenticateBearerTokenPort;

    @Test
    void createsOrderWithBearerToken() throws Exception {
        AuthenticatedUser client = client();
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(client);
        when(createOrderPort.create(any(CreateOrderRequest.class), eq(client))).thenReturn(orderResponse());

        mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateOrderRequest(
                                List.of(new CreateOrderItemRequest(20, 2)),
                                "Sin cebolla"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.id", is(10)))
                .andExpect(jsonPath("$.data.estado", is("pendiente")))
                .andExpect(jsonPath("$.data.total", is(130.00)));
    }

    @Test
    void createOrderReturnsUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateOrderRequest(
                                List.of(new CreateOrderItemRequest(20, 1)),
                                null
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void getMyOrdersUsesAuthenticatedUser() throws Exception {
        AuthenticatedUser client = client();
        OrderSummaryResponse summary = OrderSummaryResponse.from(
                new com.puravida.modules.orders.domain.model.Order(
                        10,
                        1,
                        LocalDate.of(2026, 7, 10),
                        LocalTime.of(12, 0),
                        com.puravida.modules.orders.domain.model.OrderStatus.PENDIENTE,
                        new BigDecimal("130.00"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        LocalDateTime.of(2026, 7, 10, 12, 0)
                ),
                "Cliente Prueba"
        );
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(client);
        when(getMyOrdersPort.getMyOrders(client)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/orders/my").header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id", is(10)))
                .andExpect(jsonPath("$.data[0].clienteId", is(1)));
    }

    @Test
    void getOrderReturnsForbiddenWhenUseCaseRejectsOwnership() throws Exception {
        AuthenticatedUser client = client();
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(client);
        when(getOrderDetailPort.getOrder(10, client))
                .thenThrow(new ForbiddenException("No tienes permisos para consultar este pedido."));

        mockMvc.perform(get("/api/v1/orders/10").header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    private AuthenticatedUser client() {
        return new AuthenticatedUser(1, "cliente@example.com", UserRole.CLIENTE);
    }

    private OrderResponse orderResponse() {
        return new OrderResponse(
                10,
                1,
                "Cliente Prueba",
                "pendiente",
                LocalDate.of(2026, 7, 10),
                LocalTime.of(12, 0),
                new BigDecimal("130.00"),
                "Sin cebolla",
                null,
                null,
                null,
                List.of(new OrderItemResponse(
                        50,
                        20,
                        5,
                        "Tacos",
                        2,
                        new BigDecimal("65.00"),
                        new BigDecimal("130.00")
                ))
        );
    }
}
