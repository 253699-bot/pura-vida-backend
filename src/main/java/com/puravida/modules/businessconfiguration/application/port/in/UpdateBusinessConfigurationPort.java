package com.puravida.modules.businessconfiguration.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.businessconfiguration.application.dto.BusinessConfigurationResponse;
import com.puravida.modules.businessconfiguration.application.dto.UpdateBusinessConfigurationRequest;

public interface UpdateBusinessConfigurationPort {
    BusinessConfigurationResponse update(
            UpdateBusinessConfigurationRequest request,
            AuthenticatedUser authenticatedUser
    );
}
