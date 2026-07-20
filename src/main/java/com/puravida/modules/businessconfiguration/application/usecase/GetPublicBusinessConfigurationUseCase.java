package com.puravida.modules.businessconfiguration.application.usecase;

import com.puravida.modules.businessconfiguration.application.dto.BusinessConfigurationResponse;
import com.puravida.modules.businessconfiguration.application.port.in.GetPublicBusinessConfigurationPort;
import com.puravida.modules.businessconfiguration.application.port.out.BusinessConfigurationRepositoryPort;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetPublicBusinessConfigurationUseCase implements GetPublicBusinessConfigurationPort {

    private final BusinessConfigurationRepositoryPort repositoryPort;

    public GetPublicBusinessConfigurationUseCase(BusinessConfigurationRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessConfigurationResponse get() {
        return BusinessConfigurationResponse.from(repositoryPort.findSingleton()
                .orElseThrow(() -> new NotFoundException("La configuracion del negocio no esta disponible.")));
    }
}
