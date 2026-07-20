package com.puravida.modules.businessconfiguration.web.controller;

import com.puravida.modules.businessconfiguration.application.dto.BusinessConfigurationResponse;
import com.puravida.modules.businessconfiguration.application.dto.BusinessLogoContent;
import com.puravida.modules.businessconfiguration.application.port.in.GetPublicBusinessConfigurationPort;
import com.puravida.modules.businessconfiguration.application.port.in.GetPublicBusinessLogoPort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/business/configuration")
public class BusinessConfigurationController {

    private final GetPublicBusinessConfigurationPort getConfigurationPort;
    private final GetPublicBusinessLogoPort getLogoPort;

    public BusinessConfigurationController(
            GetPublicBusinessConfigurationPort getConfigurationPort,
            GetPublicBusinessLogoPort getLogoPort
    ) {
        this.getConfigurationPort = getConfigurationPort;
        this.getLogoPort = getLogoPort;
    }

    @GetMapping
    public ApiResponse<BusinessConfigurationResponse> getConfiguration() {
        return ApiResponse.ok(getConfigurationPort.get());
    }

    @GetMapping("/logo")
    public ResponseEntity<byte[]> getLogo() {
        BusinessLogoContent logo = getLogoPort.get();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(logo.mediaType()))
                .contentLength(logo.content().length)
                .cacheControl(CacheControl.noCache().cachePublic())
                .eTag(Character.toString(34) + logo.etag() + Character.toString(34))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .body(logo.content());
    }
}
