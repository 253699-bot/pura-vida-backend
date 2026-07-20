package com.puravida.modules.businessconfiguration.application.usecase;

import com.puravida.modules.businessconfiguration.application.dto.BusinessLogoContent;
import com.puravida.modules.businessconfiguration.application.port.in.GetPublicBusinessLogoPort;
import com.puravida.modules.businessconfiguration.application.port.out.BusinessConfigurationRepositoryPort;
import com.puravida.modules.businessconfiguration.application.port.out.BusinessLogoStoragePort;
import com.puravida.modules.businessconfiguration.domain.model.BusinessConfiguration;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetPublicBusinessLogoUseCase implements GetPublicBusinessLogoPort {

    private final BusinessConfigurationRepositoryPort repositoryPort;
    private final BusinessLogoStoragePort storagePort;

    public GetPublicBusinessLogoUseCase(
            BusinessConfigurationRepositoryPort repositoryPort,
            BusinessLogoStoragePort storagePort
    ) {
        this.repositoryPort = repositoryPort;
        this.storagePort = storagePort;
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessLogoContent get() {
        BusinessConfiguration configuration = repositoryPort.findSingleton()
                .orElseThrow(() -> new NotFoundException("La configuracion del negocio no esta disponible."));
        if (configuration.logoKey() == null || configuration.logoKey().isBlank()) {
            throw new NotFoundException("El logo del negocio no esta disponible.");
        }
        return storagePort.load(configuration.logoKey());
    }
}
