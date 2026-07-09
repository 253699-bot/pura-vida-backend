package com.puravida.modules.auth.web.controller;

import com.puravida.modules.auth.application.dto.AuthResponse;
import com.puravida.modules.auth.application.dto.LoginRequest;
import com.puravida.modules.auth.application.dto.RegisterRequest;
import com.puravida.modules.auth.application.dto.UserSummaryResponse;
import com.puravida.modules.auth.application.port.in.LoginUserPort;
import com.puravida.modules.auth.application.port.in.RegisterUserPort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/auth")
public class AuthController {

    private final RegisterUserPort registerUserPort;
    private final LoginUserPort loginUserPort;

    public AuthController(RegisterUserPort registerUserPort, LoginUserPort loginUserPort) {
        this.registerUserPort = registerUserPort;
        this.loginUserPort = loginUserPort;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserSummaryResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(registerUserPort.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(loginUserPort.login(request));
    }
}
