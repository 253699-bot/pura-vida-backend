package com.puravida.modules.users.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.dto.UpdateMyProfileRequest;
import com.puravida.modules.users.application.dto.UserProfileResponse;
import com.puravida.modules.users.application.port.in.UpdateMyProfilePort;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.exception.UserProfileValidationException;
import com.puravida.modules.users.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateMyProfileUseCase implements UpdateMyProfilePort {

    private final UserRepositoryPort userRepositoryPort;
    private final UserProfileAuthorizationService authorizationService;

    public UpdateMyProfileUseCase(
            UserRepositoryPort userRepositoryPort,
            UserProfileAuthorizationService authorizationService
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public UserProfileResponse update(
            UpdateMyProfileRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        User currentUser = authorizationService.requireActiveUserForUpdate(authenticatedUser);
        validateRequest(request);

        String nombre = request.nombre() == null
                ? currentUser.nombre()
                : normalizeName(request.nombre());
        String telefono = request.telefono() == null
                ? currentUser.telefono()
                : normalizePhone(request.telefono());

        return UserProfileResponse.from(userRepositoryPort.save(currentUser.updateProfile(nombre, telefono)));
    }

    private void validateRequest(UpdateMyProfileRequest request) {
        if (request == null) {
            throw new UserProfileValidationException("Los datos del perfil son obligatorios.");
        }
        if (request.correo() != null || request.rol() != null || request.password() != null) {
            throw new UserProfileValidationException("Solo se permite actualizar nombre y telefono.");
        }
        if (request.nombre() == null && request.telefono() == null) {
            throw new UserProfileValidationException("Debes enviar nombre o telefono para actualizar el perfil.");
        }
    }

    private String normalizeName(String nombre) {
        String normalized = nombre.trim();
        if (normalized.isEmpty()) {
            throw new UserProfileValidationException("El nombre no puede estar vacio.");
        }
        return normalized;
    }

    private String normalizePhone(String telefono) {
        String normalized = telefono.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
