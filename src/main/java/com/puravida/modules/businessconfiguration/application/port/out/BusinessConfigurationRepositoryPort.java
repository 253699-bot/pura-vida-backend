package com.puravida.modules.businessconfiguration.application.port.out;

import com.puravida.modules.businessconfiguration.domain.model.BusinessConfiguration;
import java.util.Optional;

public interface BusinessConfigurationRepositoryPort {
    Optional<BusinessConfiguration> findSingleton();
    Optional<BusinessConfiguration> findSingletonForUpdate();
    BusinessConfiguration save(BusinessConfiguration configuration);
}
