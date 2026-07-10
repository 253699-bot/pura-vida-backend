package com.puravida.modules.orders.web.controller;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.orders.application.dto.CreateOrderRequest;
import com.puravida.modules.orders.application.dto.OrderResponse;
import com.puravida.modules.orders.application.dto.OrderSummaryResponse;
import com.puravida.modules.orders.application.port.in.CreateOrderPort;
import com.puravida.modules.orders.application.port.in.GetMyOrdersPort;
import com.puravida.modules.orders.application.port.in.GetOrderDetailPort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/orders")
public class OrderController {

    private final CreateOrderPort createOrderPort;
    private final GetMyOrdersPort getMyOrdersPort;
    private final GetOrderDetailPort getOrderDetailPort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public OrderController(
            CreateOrderPort createOrderPort,
            GetMyOrdersPort getMyOrdersPort,
            GetOrderDetailPort getOrderDetailPort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.createOrderPort = createOrderPort;
        this.getMyOrdersPort = getMyOrdersPort;
        this.getOrderDetailPort = getOrderDetailPort;
        this.authenticateBearerTokenPort = authenticateBearerTokenPort;
    }

    @PostMapping
    public ApiResponse<OrderResponse> create(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(createOrderPort.create(request, authenticatedUser));
    }

    @GetMapping("/my")
    public ApiResponse<List<OrderSummaryResponse>> getMyOrders(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getMyOrdersPort.getMyOrders(authenticatedUser));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrder(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getOrderDetailPort.getOrder(id, authenticatedUser));
    }
}
