package com.puravida.modules.businessconfiguration.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.businessconfiguration.application.dto.BusinessConfigurationResponse;
import com.puravida.modules.businessconfiguration.application.dto.StoredBusinessLogo;
import com.puravida.modules.businessconfiguration.application.port.in.UpdateBusinessLogoPort;
import com.puravida.modules.businessconfiguration.application.port.out.BusinessConfigurationRepositoryPort;
import com.puravida.modules.businessconfiguration.application.port.out.BusinessLogoStoragePort;
import com.puravida.modules.businessconfiguration.domain.model.BusinessConfiguration;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class UpdateBusinessLogoUseCase implements UpdateBusinessLogoPort {

    private final BusinessConfigurationRepositoryPort repositoryPort;
    private final BusinessLogoStoragePort storagePort;
    private final BusinessConfigurationAuthorizationService authorizationService;

    public UpdateBusinessLogoUseCase(
            BusinessConfigurationRepositoryPort repositoryPort,
            BusinessLogoStoragePort storagePort,
            BusinessConfigurationAuthorizationService authorizationService
    ) {
        this.repositoryPort = repositoryPort;
        this.storagePort = storagePort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public BusinessConfigurationResponse update(
            byte[] content,
            String mediaType,
            AuthenticatedUser authenticatedUser
    ) {
        User actor = authorizationService.requireEncargada(authenticatedUser);
        BusinessConfiguration current = repositoryPort.findSingletonForUpdate()
                .orElseThrow(() -> new NotFoundException("La configuracion del negocio no esta disponible."));
        StoredBusinessLogo stored = storagePort.store(content, mediaType);
        registerCleanup(stored.key(), current.logoKey());
        try {
            BusinessConfiguration updated = repositoryPort.save(current.updateLogo(stored.key(), actor.id()));
            return BusinessConfigurationResponse.from(updated);
        } catch (RuntimeException exception) {
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                deleteQuietly(stored.key());
            }
            throw exception;
        }
    }

    private void registerCleanup(String newKey, String oldKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (oldKey != null && !oldKey.equals(newKey)) {
                    deleteQuietly(oldKey);
                }
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    deleteQuietly(newKey);
                }
            }
        });
    }

    private void deleteQuietly(String key) {
        try {
            storagePort.delete(key);
        } catch (RuntimeException ignored) {
            // La limpieza best-effort no debe cambiar el resultado transaccional.
        }
    }
}
