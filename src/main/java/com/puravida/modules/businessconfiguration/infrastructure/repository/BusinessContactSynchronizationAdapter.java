package com.puravida.modules.businessconfiguration.infrastructure.repository;

import com.puravida.modules.businessconfiguration.application.port.out.BusinessConfigurationRepositoryPort;
import com.puravida.modules.businessconfiguration.domain.model.BusinessConfiguration;
import com.puravida.modules.users.application.port.out.BusinessContactSynchronizationPort;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class BusinessContactSynchronizationAdapter implements BusinessContactSynchronizationPort {

    private final BusinessConfigurationRepositoryPort configurationRepositoryPort;

    public BusinessContactSynchronizationAdapter(
            BusinessConfigurationRepositoryPort configurationRepositoryPort
    ) {
        this.configurationRepositoryPort = configurationRepositoryPort;
    }

    @Override
    public void synchronize(String correo, String telefono, Integer actorId) {
        BusinessConfiguration current = configurationRepositoryPort.findSingletonForUpdate()
                .orElseThrow(() -> new NotFoundException("La configuracion del negocio no esta disponible."));
        configurationRepositoryPort.save(current.synchronizeContact(correo, telefono, actorId));
    }
}
