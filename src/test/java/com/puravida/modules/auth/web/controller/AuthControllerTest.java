package com.puravida.modules.auth.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puravida.modules.auth.application.dto.AuthResponse;
import com.puravida.modules.auth.application.dto.LoginRequest;
import com.puravida.modules.auth.application.dto.RegisterRequest;
import com.puravida.modules.auth.application.dto.UserSummaryResponse;
import com.puravida.modules.auth.application.port.in.LoginUserPort;
import com.puravida.modules.auth.application.port.in.RegisterUserPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AuthController.class, properties = "debug=false")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUserPort registerUserPort;

    @MockitoBean
    private LoginUserPort loginUserPort;

    @Test
    void registerCreatesClientUser() throws Exception {
        UserSummaryResponse user = new UserSummaryResponse(
                1,
                "Ana Perez",
                "ana@example.com",
                "9610000000",
                "cliente",
                null,
                true,
                true
        );
        when(registerUserPort.register(any(RegisterRequest.class))).thenReturn(user);

        RegisterRequest request = new RegisterRequest(
                "Ana Perez",
                "ana@example.com",
                "9610000000",
                "password123"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.rol", is("cliente")))
                .andExpect(jsonPath("$.data.correo", is("ana@example.com")));
    }

    @Test
    void loginReturnsTokenAndUserSummary() throws Exception {
        UserSummaryResponse user = new UserSummaryResponse(
                1,
                "Ana Perez",
                "ana@example.com",
                "9610000000",
                "cliente",
                null,
                true,
                true
        );
        when(loginUserPort.login(any(LoginRequest.class)))
                .thenReturn(AuthResponse.bearer("jwt-token", 120, user));

        LoginRequest request = new LoginRequest("ana@example.com", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.token", is("jwt-token")))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.user.correo", is("ana@example.com")));
    }
}
