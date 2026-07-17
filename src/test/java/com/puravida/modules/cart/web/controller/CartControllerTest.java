package com.puravida.modules.cart.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.cart.application.dto.AddCartItemRequest;
import com.puravida.modules.cart.application.dto.CartItemResponse;
import com.puravida.modules.cart.application.dto.CartResponse;
import com.puravida.modules.cart.application.dto.UpdateCartItemQuantityRequest;
import com.puravida.modules.cart.application.port.in.AddCartItemPort;
import com.puravida.modules.cart.application.port.in.CheckoutCartPort;
import com.puravida.modules.cart.application.port.in.DeleteCartItemPort;
import com.puravida.modules.cart.application.port.in.GetCartPort;
import com.puravida.modules.cart.application.port.in.UpdateCartItemQuantityPort;
import com.puravida.modules.orders.application.dto.OrderItemResponse;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import com.puravida.shared.web.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDate;
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

@WebMvcTest(value = CartController.class, properties = "debug=false")
@Import(GlobalExceptionHandler.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetCartPort getCartPort;

    @MockitoBean
    private AddCartItemPort addCartItemPort;

    @MockitoBean
    private UpdateCartItemQuantityPort updateCartItemQuantityPort;

    @MockitoBean
    private DeleteCartItemPort deleteCartItemPort;

    @MockitoBean
    private CheckoutCartPort checkoutCartPort;

    @MockitoBean
    private AuthenticateBearerTokenPort authenticateBearerTokenPort;

    @Test
    void getCartReturnsUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postCartItemReturnsUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dishId\":2,\"cantidad\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteCartItemReturnsUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(delete("/api/v1/cart/items/8"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validatesNonPositiveQuantities() throws Exception {
        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dishId\":2,\"cantidad\":0}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/v1/cart/items/8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addsAndReturnsCartItem() throws Exception {
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(client());
        when(addCartItemPort.add(any(AddCartItemRequest.class), eq(client()))).thenReturn(itemResponse());

        mockMvc.perform(post("/api/v1/cart/items")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddCartItemRequest(2, 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(8)))
                .andExpect(jsonPath("$.data.precioUnitario", is(85.00)));
    }

    @Test
    void checkoutCreatesOrderWithDetail() throws Exception {
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(client());
        when(checkoutCartPort.checkout(client())).thenReturn(orderResponse());

        mockMvc.perform(post("/api/v1/cart/checkout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(10)))
                .andExpect(jsonPath("$.data.estado", is("pendiente")))
                .andExpect(jsonPath("$.data.total", is(180.00)))
                .andExpect(jsonPath("$.data.items[0].menuItemId", is(20)))
                .andExpect(jsonPath("$.data.items[0].platilloId", is(2)));
    }

    @Test
    void checkoutReturnsUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(post("/api/v1/cart/checkout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkoutReturnsConflictForEmptyCart() throws Exception {
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(client());
        when(checkoutCartPort.checkout(client())).thenThrow(new ConflictException("El carrito esta vacio."));

        mockMvc.perform(post("/api/v1/cart/checkout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("El carrito esta vacio.")));
    }

    @Test
    void deletesOwnedCartItemWithNoContent() throws Exception {
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(client());
        doNothing().when(deleteCartItemPort).delete(8, client());

        mockMvc.perform(delete("/api/v1/cart/items/8")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturnsNotFoundForMissingItem() throws Exception {
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(client());
        org.mockito.Mockito.doThrow(new NotFoundException("Item de carrito no encontrado."))
                .when(deleteCartItemPort).delete(99, client());

        mockMvc.perform(delete("/api/v1/cart/items/99")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    private AuthenticatedUser client() {
        return new AuthenticatedUser(1, "cliente@example.com", UserRole.CLIENTE);
    }

    private CartItemResponse itemResponse() {
        return new CartItemResponse(8, 2, "Comida corrida", 1, new BigDecimal("85.00"), new BigDecimal("85.00"));
    }

    private OrderResponse orderResponse() {
        OrderItemResponse item = new OrderItemResponse(
                30, 20, 2, "Comida corrida", 2, new BigDecimal("90.00"), new BigDecimal("180.00")
        );
        return new OrderResponse(
                10, 1, "Cliente", "pendiente", LocalDate.now(), LocalTime.NOON,
                new BigDecimal("180.00"), null, null, null, null, List.of(item)
        );
    }
}
