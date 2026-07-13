package com.puravida.modules.sales.web.controller;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.sales.application.dto.CancelSaleRequest;
import com.puravida.modules.sales.application.dto.CreateManualSaleRequest;
import com.puravida.modules.sales.application.dto.SaleResponse;
import com.puravida.modules.sales.application.port.in.CancelSalePort;
import com.puravida.modules.sales.application.port.in.CreateManualSalePort;
import com.puravida.modules.sales.application.port.in.GetSalesPort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/admin/sales")
public class AdminSalesController {

    private final CreateManualSalePort createManualSalePort;
    private final GetSalesPort getSalesPort;
    private final CancelSalePort cancelSalePort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public AdminSalesController(
            CreateManualSalePort createManualSalePort,
            GetSalesPort getSalesPort,
            CancelSalePort cancelSalePort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.createManualSalePort = createManualSalePort;
        this.getSalesPort = getSalesPort;
        this.cancelSalePort = cancelSalePort;
        this.authenticateBearerTokenPort = authenticateBearerTokenPort;
    }

    @PostMapping("/manual")
    public ApiResponse<SaleResponse> createManualSale(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody CreateManualSaleRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(createManualSalePort.create(request, authenticatedUser));
    }

    @GetMapping
    public ApiResponse<List<SaleResponse>> getSales(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "fuente", required = false) String source,
            @RequestParam(value = "estado", required = false) String status
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getSalesPort.getSales(from, to, source, status, authenticatedUser));
    }

    @PatchMapping("/{saleId}/cancel")
    public ApiResponse<SaleResponse> cancelSale(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("saleId") Integer saleId,
            @Valid @RequestBody CancelSaleRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(cancelSalePort.cancel(saleId, request, authenticatedUser));
    }
}
