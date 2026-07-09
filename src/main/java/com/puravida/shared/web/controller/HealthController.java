package com.puravida.shared.web.controller;

import com.puravida.shared.web.ApiPaths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/health")
public class HealthController {

    private static final String PROJECT_NAME = "PuraVida";

    @GetMapping
    public HealthResponse check() {
        return new HealthResponse("OK", PROJECT_NAME);
    }

    public record HealthResponse(String status, String project) {
    }
}
