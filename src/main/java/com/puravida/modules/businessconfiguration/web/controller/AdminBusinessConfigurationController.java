package com.puravida.modules.businessconfiguration.web.controller;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.businessconfiguration.application.dto.BusinessConfigurationResponse;
import com.puravida.modules.businessconfiguration.application.dto.UpdateBusinessConfigurationRequest;
import com.puravida.modules.businessconfiguration.application.port.in.UpdateBusinessConfigurationPort;
import com.puravida.modules.businessconfiguration.application.port.in.UpdateBusinessLogoPort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/admin/business/configuration")
public class AdminBusinessConfigurationController {

    private final UpdateBusinessConfigurationPort updateConfigurationPort;
    private final UpdateBusinessLogoPort updateLogoPort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public AdminBusinessConfigurationController(
            UpdateBusinessConfigurationPort updateConfigurationPort,
            UpdateBusinessLogoPort updateLogoPort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.updateConfigurationPort = updateConfigurationPort;
        this.updateLogoPort = updateLogoPort;
        this.authenticateBearerTokenPort = authenticateBearerTokenPort;
    }

    @PatchMapping
    public ApiResponse<BusinessConfigurationResponse> updateConfiguration(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody UpdateBusinessConfigurationRequest request
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(updateConfigurationPort.update(request, authenticatedUser));
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BusinessConfigurationResponse> updateLogo(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestPart("file") MultipartFile file
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        try {
            return ApiResponse.ok(updateLogoPort.update(
                    file.getBytes(), file.getContentType(), authenticatedUser
            ));
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer el archivo de logo.", exception);
        }
    }
}
