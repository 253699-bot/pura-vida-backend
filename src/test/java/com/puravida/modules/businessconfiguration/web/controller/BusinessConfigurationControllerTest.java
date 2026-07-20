package com.puravida.modules.businessconfiguration.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.businessconfiguration.application.dto.BusinessConfigurationResponse;
import com.puravida.modules.businessconfiguration.application.dto.BusinessLogoContent;
import com.puravida.modules.businessconfiguration.application.dto.UpdateBusinessConfigurationRequest;
import com.puravida.modules.businessconfiguration.application.port.in.GetPublicBusinessConfigurationPort;
import com.puravida.modules.businessconfiguration.application.port.in.GetPublicBusinessLogoPort;
import com.puravida.modules.businessconfiguration.application.port.in.UpdateBusinessConfigurationPort;
import com.puravida.modules.businessconfiguration.application.port.in.UpdateBusinessLogoPort;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.web.GlobalExceptionHandler;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = {BusinessConfigurationController.class, AdminBusinessConfigurationController.class},
        properties = "debug=false"
)
@Import(GlobalExceptionHandler.class)
class BusinessConfigurationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean GetPublicBusinessConfigurationPort getConfigurationPort;
    @MockitoBean GetPublicBusinessLogoPort getLogoPort;
    @MockitoBean UpdateBusinessConfigurationPort updateConfigurationPort;
    @MockitoBean UpdateBusinessLogoPort updateLogoPort;
    @MockitoBean AuthenticateBearerTokenPort authenticateBearerTokenPort;

    @Test
    void exposesPublicConfigurationAndCurrentLogo() throws Exception {
        when(getConfigurationPort.get()).thenReturn(response());
        when(getLogoPort.get()).thenReturn(new BusinessLogoContent(
                new byte[]{1, 2, 3}, "image/png", "abc123"
        ));

        mockMvc.perform(get("/api/v1/business/configuration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombreFonda", is("PuraVida")))
                .andExpect(jsonPath("$.data.logoUrl", is("/api/v1/business/configuration/logo")));

        mockMvc.perform(get("/api/v1/business/configuration/logo"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{1, 2, 3}))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void letsAuthenticatedEncargadaPatchAndUploadLogo() throws Exception {
        AuthenticatedUser actor = new AuthenticatedUser(4, "admin@example.com", UserRole.ENCARGADA);
        when(authenticateBearerTokenPort.authenticate("Bearer admin-token")).thenReturn(actor);
        when(updateConfigurationPort.update(any(UpdateBusinessConfigurationRequest.class), eq(actor)))
                .thenReturn(response());
        when(updateLogoPort.update(any(byte[].class), eq("image/png"), eq(actor)))
                .thenReturn(response());

        mockMvc.perform(patch("/api/v1/admin/business/configuration")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateBusinessConfigurationRequest(
                                        "PuraVida", null, null, null, null
                                )
                        )))
                .andExpect(status().isOk());

        MockMultipartFile file = new MockMultipartFile(
                "file", "ignored.png", "image/png", new byte[]{1, 2, 3}
        );
        mockMvc.perform(multipart("/api/v1/admin/business/configuration/logo")
                        .file(file)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.logoUrl", is("/api/v1/business/configuration/logo")));
    }

    private BusinessConfigurationResponse response() {
        return new BusinessConfigurationResponse(
                "PuraVida", "/api/v1/business/configuration/logo", "Calle Central",
                "Lunes a viernes", "9610000000", "admin@example.com",
                LocalDateTime.of(2026, 7, 18, 10, 0)
        );
    }
}
