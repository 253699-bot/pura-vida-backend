package com.puravida.modules.business.web.controller;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.business.application.dto.TodayBusinessStatusResponse;
import com.puravida.modules.business.application.dto.UpdateTodayBusinessStatusRequest;
import com.puravida.modules.business.application.port.in.GetTodayBusinessStatusPort;
import com.puravida.modules.business.application.port.in.UpdateTodayBusinessStatusPort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/business/status")
public class BusinessStatusController {

    private final GetTodayBusinessStatusPort getTodayBusinessStatusPort;
    private final UpdateTodayBusinessStatusPort updateTodayBusinessStatusPort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public BusinessStatusController(
            GetTodayBusinessStatusPort getTodayBusinessStatusPort,
            UpdateTodayBusinessStatusPort updateTodayBusinessStatusPort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.getTodayBusinessStatusPort = getTodayBusinessStatusPort;
        this.updateTodayBusinessStatusPort = updateTodayBusinessStatusPort;
        this.authenticateBearerTokenPort = authenticateBearerTokenPort;
    }

    @GetMapping("/today")
    public ApiResponse<TodayBusinessStatusResponse> getToday() {
        return ApiResponse.ok(getTodayBusinessStatusPort.getToday());
    }

    @PutMapping("/today")
    public ApiResponse<TodayBusinessStatusResponse> updateToday(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody UpdateTodayBusinessStatusRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(updateTodayBusinessStatusPort.updateToday(request, authenticatedUser));
    }
}
