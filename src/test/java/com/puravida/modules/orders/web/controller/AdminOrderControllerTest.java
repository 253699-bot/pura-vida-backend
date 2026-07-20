package com.puravida.modules.orders.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.orders.application.dto.AcceptOrderRequest;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.dto.OrderSummaryResponse;
import com.puravida.modules.orders.application.dto.RejectOrderRequest;
import com.puravida.modules.orders.application.port.in.AcceptOrderPort;
import com.puravida.modules.orders.application.port.in.CancelOrderPort;
import com.puravida.modules.orders.application.port.in.CompleteOrderPort;
import com.puravida.modules.orders.application.port.in.GetAdminOrderDetailPort;
import com.puravida.modules.orders.application.port.in.GetAdminOrdersPort;
import com.puravida.modules.orders.application.port.in.RejectOrderPort;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.NotFoundException;
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

@WebMvcTest(value = AdminOrderController.class, properties = "debug=false")
@Import(GlobalExceptionHandler.class)
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetAdminOrdersPort getAdminOrdersPort;

    @MockitoBean
    private GetAdminOrderDetailPort getAdminOrderDetailPort;

    @MockitoBean
    private AcceptOrderPort acceptOrderPort;

    @MockitoBean
    private RejectOrderPort rejectOrderPort;

    @MockitoBean
    private CompleteOrderPort completeOrderPort;

    @MockitoBean
    private CancelOrderPort cancelOrderPort;

    @MockitoBean
    private AuthenticateBearerTokenPort authenticateBearerTokenPort;

    @Test
    void listsOrdersForEncargadaWithStatusFilter() throws Exception {
        AuthenticatedUser encargada = encargada();
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(getAdminOrdersPort.getOrders("pendiente", false, false, encargada))
                .thenReturn(List.of(OrderSummaryResponse.from(pendingOrder(), "Cliente Prueba")));

        mockMvc.perform(get("/api/v1/admin/orders?estado=pendiente")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].estado", is("pendiente")))
                .andExpect(jsonPath("$.data[0].clienteNombre", is("Cliente Prueba")));
    }

    @Test
    void listsHistoricalOrdersForEncargada() throws Exception {
        AuthenticatedUser encargada = encargada();
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(getAdminOrdersPort.getOrders(null, false, true, encargada))
                .thenReturn(List.of(OrderSummaryResponse.from(pendingOrder().complete(), "Cliente Prueba")));

        mockMvc.perform(get("/api/v1/admin/orders?historyOnly=true")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].estado", is("finalizado")));
    }

    @Test
    void acceptsOrderForEncargada() throws Exception {
        AuthenticatedUser encargada = encargada();
        OrderResponse response = orderResponse(OrderStatus.ACEPTADO, null);
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(acceptOrderPort.accept(eq(10), any(AcceptOrderRequest.class), eq(encargada)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/orders/10/accept")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AcceptOrderRequest("25 minutos")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado", is("aceptado")))
                .andExpect(jsonPath("$.data.tiempoEsperaEstimado", is("25 minutos")));
    }

    @Test
    void getsAdminOrderDetail() throws Exception {
        AuthenticatedUser encargada = encargada();
        OrderResponse response = orderResponse(OrderStatus.ACEPTADO, null);
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(getAdminOrderDetailPort.getOrder(10, encargada)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/orders/10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(10)))
                .andExpect(jsonPath("$.data.tiempoEsperaEstimado", is("25 minutos")));
    }

    @Test
    void cancelsAcceptedOrderWithoutRequestBody() throws Exception {
        AuthenticatedUser encargada = encargada();
        OrderResponse response = orderResponse(OrderStatus.CANCELADO, null);
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(cancelOrderPort.cancel(10, encargada)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/orders/10/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado", is("cancelado")))
                .andExpect(jsonPath("$.data.canceladoPor", is(2)));
    }

    @Test
    void acceptRejectsEstimatedWaitLongerThanOneHundredCharacters() throws Exception {
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada());

        mockMvc.perform(patch("/api/v1/admin/orders/10/accept")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AcceptOrderRequest("x".repeat(101))
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.tiempoEsperaEstimado").exists());
    }

    @Test
    void rejectsOrderForEncargada() throws Exception {
        AuthenticatedUser encargada = encargada();
        OrderResponse response = orderResponse(OrderStatus.RECHAZADO, "No hay tortillas");
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(rejectOrderPort.reject(eq(10), any(RejectOrderRequest.class), eq(encargada))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/orders/10/reject")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RejectOrderRequest("No hay tortillas"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado", is("rechazado")))
                .andExpect(jsonPath("$.data.motivoRechazo", is("No hay tortillas")));
    }

    @Test
    void completesAcceptedOrderForEncargada() throws Exception {
        AuthenticatedUser encargada = encargada();
        OrderResponse response = orderResponse(OrderStatus.FINALIZADO, null);
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(completeOrderPort.complete(10, encargada)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/orders/10/complete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado", is("finalizado")));
    }

    @Test
    void completeReturnsConflictForInvalidTransition() throws Exception {
        AuthenticatedUser encargada = encargada();
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(completeOrderPort.complete(10, encargada))
                .thenThrow(new ConflictException("Solo los pedidos aceptados pueden finalizarse."));

        mockMvc.perform(patch("/api/v1/admin/orders/10/complete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Solo los pedidos aceptados pueden finalizarse.")));
    }

    @Test
    void completeReturnsNotFoundForMissingOrder() throws Exception {
        AuthenticatedUser encargada = encargada();
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(completeOrderPort.complete(999, encargada))
                .thenThrow(new NotFoundException("No se encontro el pedido."));

        mockMvc.perform(patch("/api/v1/admin/orders/999/complete")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("No se encontro el pedido.")));
    }

    @Test
    void adminEndpointsReturnUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void acceptReturnsForbiddenForClientRole() throws Exception {
        AuthenticatedUser client = new AuthenticatedUser(1, "cliente@example.com", UserRole.CLIENTE);
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(client);
        when(acceptOrderPort.accept(eq(10), any(AcceptOrderRequest.class), eq(client)))
                .thenThrow(new ForbiddenException("No tienes permisos para administrar pedidos."));

        mockMvc.perform(patch("/api/v1/admin/orders/10/accept")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AcceptOrderRequest("25 minutos")
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    private AuthenticatedUser encargada() {
        return new AuthenticatedUser(2, "encargada@example.com", UserRole.ENCARGADA);
    }

    private Order pendingOrder() {
        return new Order(
                10,
                1,
                LocalDate.of(2026, 7, 10),
                LocalTime.of(12, 0),
                OrderStatus.PENDIENTE,
                new BigDecimal("130.00"),
                null,
                null,
                null,
                null,
                null,
                null,
                "Sin cebolla",
                LocalDateTime.of(2026, 7, 10, 12, 0)
        );
    }

    private OrderResponse orderResponse(OrderStatus status, String motivoRechazo) {
        Order order = pendingOrder();
        return new OrderResponse(
                order.id(),
                order.clienteId(),
                "Cliente Prueba",
                status.databaseValue(),
                order.fecha(),
                order.hora(),
                order.total(),
                order.observaciones(),
                motivoRechazo,
                status == OrderStatus.PENDIENTE ? null : 2,
                status == OrderStatus.PENDIENTE ? null : LocalDateTime.of(2026, 7, 10, 12, 5),
                status == OrderStatus.PENDIENTE ? null : "25 minutos",
                status == OrderStatus.CANCELADO ? 2 : null,
                status == OrderStatus.CANCELADO ? LocalDateTime.of(2026, 7, 10, 12, 15) : null,
                List.of()
        );
    }
}
