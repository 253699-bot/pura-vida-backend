package com.puravida.modules.cart.web.controller;

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
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/cart")
public class CartController {

    private final GetCartPort getCartPort;
    private final AddCartItemPort addCartItemPort;
    private final UpdateCartItemQuantityPort updateCartItemQuantityPort;
    private final DeleteCartItemPort deleteCartItemPort;
    private final CheckoutCartPort checkoutCartPort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public CartController(
            GetCartPort getCartPort,
            AddCartItemPort addCartItemPort,
            UpdateCartItemQuantityPort updateCartItemQuantityPort,
            DeleteCartItemPort deleteCartItemPort,
            CheckoutCartPort checkoutCartPort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.getCartPort = getCartPort;
        this.addCartItemPort = addCartItemPort;
        this.updateCartItemQuantityPort = updateCartItemQuantityPort;
        this.deleteCartItemPort = deleteCartItemPort;
        this.checkoutCartPort = checkoutCartPort;
        this.authenticateBearerTokenPort = authenticateBearerTokenPort;
    }

    @GetMapping
    public ApiResponse<CartResponse> getCart(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        AuthenticatedUser user = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getCartPort.getCart(user));
    }

    @PostMapping("/items")
    public ApiResponse<CartItemResponse> addItem(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        AuthenticatedUser user = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(addCartItemPort.add(request, user));
    }

    @PostMapping("/checkout")
    public ApiResponse<OrderResponse> checkout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        AuthenticatedUser user = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(checkoutCartPort.checkout(user));
    }

    @PatchMapping("/items/{cartItemId}")
    public ApiResponse<CartItemResponse> updateItemQuantity(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable Integer cartItemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        AuthenticatedUser user = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(updateCartItemQuantityPort.updateQuantity(cartItemId, request, user));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> deleteItem(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable Integer cartItemId
    ) {
        AuthenticatedUser user = authenticateBearerTokenPort.authenticate(authorizationHeader);
        deleteCartItemPort.delete(cartItemId, user);
        return ResponseEntity.noContent().build();
    }
}
