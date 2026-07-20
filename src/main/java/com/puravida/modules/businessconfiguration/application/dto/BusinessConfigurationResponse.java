package com.puravida.modules.businessconfiguration.application.dto;

import com.puravida.modules.businessconfiguration.domain.model.BusinessConfiguration;
import com.puravida.shared.web.ApiPaths;
import java.time.LocalDateTime;

public record BusinessConfigurationResponse(
        String nombreFonda,
        String logoUrl,
        String direccion,
        String horarios,
        String telefono,
        String correo,
        LocalDateTime actualizadoEn
) {
    private static final String PUBLIC_LOGO_URL = ApiPaths.API_V1 + "/business/configuration/logo";

    public static BusinessConfigurationResponse from(BusinessConfiguration configuration) {
        String logoUrl = configuration.logoKey() == null || configuration.logoKey().isBlank()
                ? null
                : PUBLIC_LOGO_URL;
        return new BusinessConfigurationResponse(
                configuration.nombreFonda(), logoUrl, configuration.direccion(),
                configuration.horarios(), configuration.telefono(), configuration.correo(),
                configuration.actualizadoEn()
        );
    }
}
