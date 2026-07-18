package com.puravida.modules.users.web.controller;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.users.application.dto.UpdateMyProfileRequest;
import com.puravida.modules.users.application.dto.UserProfileResponse;
import com.puravida.modules.users.application.port.in.GetMyProfilePort;
import com.puravida.modules.users.application.port.in.UpdateMyProfilePort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/me")
public class MeController {

    private final GetMyProfilePort getMyProfilePort;
    private final UpdateMyProfilePort updateMyProfilePort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public MeController(
            GetMyProfilePort getMyProfilePort,
            UpdateMyProfilePort updateMyProfilePort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.getMyProfilePort = getMyProfilePort;
        this.updateMyProfilePort = updateMyProfilePort;
        this.authenticateBearerTokenPort = authenticateBearerTokenPort;
    }

    @GetMapping
    public ApiResponse<UserProfileResponse> get(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getMyProfilePort.get(authenticatedUser));
    }

    @PatchMapping
    public ApiResponse<UserProfileResponse> update(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(updateMyProfilePort.update(request, authenticatedUser));
    }
}
