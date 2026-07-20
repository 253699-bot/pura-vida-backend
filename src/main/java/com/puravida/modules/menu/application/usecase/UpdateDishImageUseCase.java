package com.puravida.modules.menu.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.DishResponse;
import com.puravida.modules.menu.application.dto.StoredDishImage;
import com.puravida.modules.menu.application.port.in.UpdateDishImagePort;
import com.puravida.modules.menu.application.port.out.DishImageStoragePort;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class UpdateDishImageUseCase implements UpdateDishImagePort {

    private final DishRepositoryPort dishRepositoryPort;
    private final DishImageStoragePort storagePort;
    private final MenuAuthorizationService authorizationService;

    public UpdateDishImageUseCase(
            DishRepositoryPort dishRepositoryPort,
            DishImageStoragePort storagePort,
            MenuAuthorizationService authorizationService
    ) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.storagePort = storagePort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public DishResponse update(
            Integer dishId,
            byte[] content,
            String mediaType,
            AuthenticatedUser authenticatedUser
    ) {
        authorizationService.requireEncargada(authenticatedUser);
        Dish current = dishRepositoryPort.findByIdForUpdate(dishId)
                .orElseThrow(() -> new NotFoundException("Platillo no encontrado."));
        if (!current.activo()) {
            throw new ConflictException("No se puede actualizar la imagen de un platillo retirado.");
        }

        StoredDishImage stored = storagePort.store(content, mediaType);
        registerCleanup(stored.key(), current.imagenKey());
        try {
            Dish updated = dishRepositoryPort.save(current.updateImage(stored.key()));
            return DishResponse.from(updated);
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