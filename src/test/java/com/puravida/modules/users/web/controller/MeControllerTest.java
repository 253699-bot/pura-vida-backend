package com.puravida.modules.users.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.users.application.dto.UpdateMyProfileRequest;
import com.puravida.modules.users.application.dto.UserProfileResponse;
import com.puravida.modules.users.application.port.in.GetMyProfilePort;
import com.puravida.modules.users.application.port.in.UpdateMyProfilePort;
import com.puravida.modules.users.domain.exception.UserProfileValidationException;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.UnauthorizedException;
import com.puravida.shared.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = MeController.class, properties = "debug=false")
@Import(GlobalExceptionHandler.class)
class MeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetMyProfilePort getMyProfilePort;

    @MockitoBean
    private UpdateMyProfilePort updateMyProfilePort;

    @MockitoBean
    private AuthenticateBearerTokenPort authenticateBearerTokenPort;

    @Test
    void getsAuthenticatedUsersProfileWithoutPassword() throws Exception {
        AuthenticatedUser authenticatedUser = authenticatedClient();
        when(authenticateBearerTokenPort.authenticate("Bearer user-token")).thenReturn(authenticatedUser);
        when(getMyProfilePort.get(authenticatedUser)).thenReturn(profile("Ana Perez", "9610000000"));

        mockMvc.perform(get("/api/v1/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(7)))
                .andExpect(jsonPath("$.data.correo", is("ana@example.com")))
                .andExpect(jsonPath("$.data.rol", is("cliente")))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void updatesAuthenticatedUsersNameAndPhone() throws Exception {
        AuthenticatedUser authenticatedUser = authenticatedClient();
        when(authenticateBearerTokenPort.authenticate("Bearer user-token")).thenReturn(authenticatedUser);
        when(updateMyProfilePort.update(any(UpdateMyProfileRequest.class), eq(authenticatedUser)))
                .thenReturn(profile("Ana Maria", "9611111111"));

        UpdateMyProfileRequest request = new UpdateMyProfileRequest(
                "Ana Maria",
                "9611111111",
                null,
                null,
                null
        );

        mockMvc.perform(patch("/api/v1/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre", is("Ana Maria")))
                .andExpect(jsonPath("$.data.telefono", is("9611111111")))
                .andExpect(jsonPath("$.data.rol", is("cliente")));
    }

    @Test
    void returnsUnauthorizedWithoutBearerToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void rejectsProtectedProfileFields() throws Exception {
        AuthenticatedUser authenticatedUser = authenticatedClient();
        when(authenticateBearerTokenPort.authenticate("Bearer user-token")).thenReturn(authenticatedUser);
        when(updateMyProfilePort.update(any(UpdateMyProfileRequest.class), eq(authenticatedUser)))
                .thenThrow(new UserProfileValidationException(
                        "Solo se permite actualizar nombre y telefono."
                ));

        UpdateMyProfileRequest request = new UpdateMyProfileRequest(
                "Ana Maria",
                null,
                null,
                "encargada",
                "new-password"
        );

        mockMvc.perform(patch("/api/v1/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Solo se permite actualizar nombre y telefono.")));
    }

    private AuthenticatedUser authenticatedClient() {
        return new AuthenticatedUser(7, "ana@example.com", UserRole.CLIENTE);
    }

    private UserProfileResponse profile(String nombre, String telefono) {
        return new UserProfileResponse(
                7,
                nombre,
                "ana@example.com",
                telefono,
                "cliente",
                null,
                true,
                true
        );
    }
}
