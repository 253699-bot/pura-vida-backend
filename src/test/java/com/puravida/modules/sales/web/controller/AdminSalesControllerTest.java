package com.puravida.modules.sales.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.sales.application.dto.CancelSaleRequest;
import com.puravida.modules.sales.application.dto.CreateManualSaleItemRequest;
import com.puravida.modules.sales.application.dto.CreateManualSaleRequest;
import com.puravida.modules.sales.application.dto.SaleItemResponse;
import com.puravida.modules.sales.application.dto.SaleResponse;
import com.puravida.modules.sales.application.port.in.CancelSalePort;
import com.puravida.modules.sales.application.port.in.CreateManualSalePort;
import com.puravida.modules.sales.application.port.in.GetSalesPort;
import com.puravida.modules.sales.domain.exception.SaleValidationException;
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

@WebMvcTest(value = AdminSalesController.class, properties = "debug=false")
@Import(GlobalExceptionHandler.class)
class AdminSalesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateManualSalePort createManualSalePort;

    @MockitoBean
    private GetSalesPort getSalesPort;

    @MockitoBean
    private CancelSalePort cancelSalePort;

    @MockitoBean
    private AuthenticateBearerTokenPort authenticateBearerTokenPort;

    @Test
    void createsManualSaleForEncargada() throws Exception {
        AuthenticatedUser encargada = encargada();
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(createManualSalePort.create(eq("sale-key"), any(CreateManualSaleRequest.class), eq(encargada)))
                .thenReturn(activeSale());

        mockMvc.perform(post("/api/v1/admin/sales/manual")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .header("Idempotency-Key", "sale-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateManualSaleRequest(
                                        List.of(new CreateManualSaleItemRequest(10, 5)),
                                        "Venta mostrador"
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.fuente", is("manual_fonda")))
                .andExpect(jsonPath("$.data.pedidoId").doesNotExist())
                .andExpect(jsonPath("$.data.total", is(125.00)))
                .andExpect(jsonPath("$.data.items[0].menuItemId", is(10)))
                .andExpect(jsonPath("$.data.items[0].precioUnitario", is(25.00)));
    }

    @Test
    void listsSalesUsingFilters() throws Exception {
        AuthenticatedUser encargada = encargada();
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(getSalesPort.getSales(
                "2026-07-01",
                "2026-07-31",
                "manual_fonda",
                "activa",
                encargada
        )).thenReturn(List.of(activeSale()));

        mockMvc.perform(get("/api/v1/admin/sales")
                        .queryParam("from", "2026-07-01")
                        .queryParam("to", "2026-07-31")
                        .queryParam("fuente", "manual_fonda")
                        .queryParam("estado", "activa")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id", is(20)))
                .andExpect(jsonPath("$.data[0].estado", is("activa")));
    }

    @Test
    void cancelsSaleLogically() throws Exception {
        AuthenticatedUser encargada = encargada();
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(cancelSalePort.cancel(eq(20), any(CancelSaleRequest.class), eq(encargada)))
                .thenReturn(cancelledSale());

        mockMvc.perform(patch("/api/v1/admin/sales/20/cancel")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CancelSaleRequest("Captura duplicada"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado", is("anulada")))
                .andExpect(jsonPath("$.data.motivoAnulacion", is("Captura duplicada")));
    }

    @Test
    void returnsUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(get("/api/v1/admin/sales"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void returnsForbiddenForClientRole() throws Exception {
        AuthenticatedUser client = new AuthenticatedUser(1, "cliente@example.com", UserRole.CLIENTE);
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(client);
        when(getSalesPort.getSales(null, null, null, null, client))
                .thenThrow(new ForbiddenException("No tienes permisos para administrar ventas."));

        mockMvc.perform(get("/api/v1/admin/sales")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void validatesPositiveManualSaleQuantity() throws Exception {
        mockMvc.perform(post("/api/v1/admin/sales/manual")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .header("Idempotency-Key", "sale-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateManualSaleRequest(
                                        List.of(new CreateManualSaleItemRequest(10, 0)),
                                        null
                                )
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void rejectsManualSaleWithoutIdempotencyKey() throws Exception {
        AuthenticatedUser encargada = encargada();
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(encargada);
        when(createManualSalePort.create(
                eq((String) null),
                any(CreateManualSaleRequest.class),
                eq(encargada)
        )).thenThrow(new SaleValidationException("El header Idempotency-Key es obligatorio."));

        mockMvc.perform(post("/api/v1/admin/sales/manual")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"menuItemId":10,"cantidad":1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void rejectsClientProvidedTotal() throws Exception {
        mockMvc.perform(post("/api/v1/admin/sales/manual")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .header("Idempotency-Key", "sale-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [{"menuItemId": 10, "cantidad": 1}],
                                  "observaciones": "mostrador",
                                  "total": 0.01
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void returnsUnauthorizedWhenCreatingManualSaleWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(post("/api/v1/admin/sales/manual")
                        .header("Idempotency-Key", "sale-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"menuItemId":10,"cantidad":1}]}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    private AuthenticatedUser encargada() {
        return new AuthenticatedUser(4, "encargada@example.com", UserRole.ENCARGADA);
    }

    private SaleResponse activeSale() {
        return new SaleResponse(
                20,
                null,
                "manual_fonda",
                "activa",
                LocalDate.of(2026, 7, 11),
                LocalTime.of(12, 0),
                new BigDecimal("125.00"),
                4,
                "Venta mostrador",
                null,
                null,
                null,
                LocalDateTime.of(2026, 7, 11, 12, 0),
                List.of(new SaleItemResponse(
                        30,
                        10,
                        110,
                        "Platillo 10",
                        5,
                        new BigDecimal("25.00"),
                        new BigDecimal("125.00")
                ))
        );
    }

    private SaleResponse cancelledSale() {
        SaleResponse sale = activeSale();
        return new SaleResponse(
                sale.id(),
                sale.pedidoId(),
                sale.fuente(),
                "anulada",
                sale.fecha(),
                sale.hora(),
                sale.total(),
                sale.registradoPor(),
                sale.observaciones(),
                "Captura duplicada",
                LocalDateTime.of(2026, 7, 11, 12, 30),
                4,
                sale.creadoEn()
        );
    }
}
