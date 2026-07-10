package com.puravida.modules.menu.web.controller;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.menu.application.dto.TodayMenuItemResponse;
import com.puravida.modules.menu.application.dto.TodayMenuResponse;
import com.puravida.modules.menu.application.dto.UpdateMenuItemAvailabilityRequest;
import com.puravida.modules.menu.application.dto.UpdateTodayMenuRequest;
import com.puravida.modules.menu.application.port.in.GetTodayMenuPort;
import com.puravida.modules.menu.application.port.in.UpdateTodayMenuItemAvailabilityPort;
import com.puravida.modules.menu.application.port.in.UpdateTodayMenuPort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/menu")
public class MenuController {

    private final GetTodayMenuPort getTodayMenuPort;
    private final UpdateTodayMenuPort updateTodayMenuPort;
    private final UpdateTodayMenuItemAvailabilityPort updateTodayMenuItemAvailabilityPort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public MenuController(
            GetTodayMenuPort getTodayMenuPort,
            UpdateTodayMenuPort updateTodayMenuPort,
            UpdateTodayMenuItemAvailabilityPort updateTodayMenuItemAvailabilityPort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.getTodayMenuPort = getTodayMenuPort;
        this.updateTodayMenuPort = updateTodayMenuPort;
        this.updateTodayMenuItemAvailabilityPort = updateTodayMenuItemAvailabilityPort;
        this.authenticateBearerTokenPort = authenticateBearerTokenPort;
    }

    @GetMapping("/today")
    public ApiResponse<TodayMenuResponse> getToday() {
        return ApiResponse.ok(getTodayMenuPort.getToday());
    }

    @PutMapping("/today")
    public ApiResponse<TodayMenuResponse> updateToday(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody UpdateTodayMenuRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(updateTodayMenuPort.updateToday(request, authenticatedUser));
    }

    @PatchMapping("/today/items/{id}/availability")
    public ApiResponse<TodayMenuItemResponse> updateAvailability(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("id") Integer id,
            @Valid @RequestBody UpdateMenuItemAvailabilityRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(updateTodayMenuItemAvailabilityPort.updateAvailability(id, request, authenticatedUser));
    }
}
