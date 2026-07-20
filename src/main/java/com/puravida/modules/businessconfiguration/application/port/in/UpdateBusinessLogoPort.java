package com.puravida.modules.businessconfiguration.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.businessconfiguration.application.dto.BusinessConfigurationResponse;

public interface UpdateBusinessLogoPort {
    BusinessConfigurationResponse update(byte[] content, String mediaType, AuthenticatedUser authenticatedUser);
}
