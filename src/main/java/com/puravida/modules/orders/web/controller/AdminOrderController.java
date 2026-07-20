package com.puravida.modules.orders.web.controller;

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
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/admin/orders")
public class AdminOrderController {

    private final GetAdminOrdersPort getAdminOrdersPort;
    private final GetAdminOrderDetailPort getAdminOrderDetailPort;
    private final AcceptOrderPort acceptOrderPort;
    private final RejectOrderPort rejectOrderPort;
    private final CompleteOrderPort completeOrderPort;
    private final CancelOrderPort cancelOrderPort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public AdminOrderController(
            GetAdminOrdersPort getAdminOrdersPort,
            GetAdminOrderDetailPort getAdminOrderDetailPort,
            AcceptOrderPort acceptOrderPort,
            RejectOrderPort rejectOrderPort,
            CompleteOrderPort completeOrderPort,
            CancelOrderPort cancelOrderPort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.getAdminOrdersPort = getAdminOrdersPort;
        this.getAdminOrderDetailPort = getAdminOrderDetailPort;
        this.acceptOrderPort = acceptOrderPort;
        this.rejectOrderPort = rejectOrderPort;
        this.completeOrderPort = completeOrderPort;
        this.cancelOrderPort = cancelOrderPort;
        this.authenticateBearerTokenPort = authenticateBearerTokenPort;
    }

    @GetMapping
    public ApiResponse<List<OrderSummaryResponse>> getOrders(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "currentCycleOnly", defaultValue = "false") boolean currentCycleOnly,
            @RequestParam(value = "historyOnly", defaultValue = "false") boolean historyOnly
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getAdminOrdersPort.getOrders(estado, currentCycleOnly, historyOnly, authenticatedUser));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrder(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getAdminOrderDetailPort.getOrder(id, authenticatedUser));
    }

    @PatchMapping("/{id}/accept")
    public ApiResponse<OrderResponse> accept(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("id") Integer id,
            @Valid @RequestBody AcceptOrderRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(acceptOrderPort.accept(id, request, authenticatedUser));
    }

    @PatchMapping("/{id}/reject")
    public ApiResponse<OrderResponse> reject(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("id") Integer id,
            @Valid @RequestBody RejectOrderRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(rejectOrderPort.reject(id, request, authenticatedUser));
    }

    @PatchMapping("/{id}/complete")
    public ApiResponse<OrderResponse> complete(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(completeOrderPort.complete(id, authenticatedUser));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(cancelOrderPort.cancel(id, authenticatedUser));
    }
}