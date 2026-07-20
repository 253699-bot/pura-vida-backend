package com.puravida.modules.users.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.dto.UpdateMyProfileRequest;
import com.puravida.modules.users.application.dto.UserProfileResponse;
import com.puravida.modules.users.application.port.in.UpdateMyProfilePort;
import com.puravida.modules.users.application.port.out.BusinessContactSynchronizationPort;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.exception.UserProfileValidationException;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ConflictException;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateMyProfileUseCase implements UpdateMyProfilePort {

    private final UserRepositoryPort userRepositoryPort;
    private final UserProfileAuthorizationService authorizationService;
    private final BusinessContactSynchronizationPort contactSynchronizationPort;

    public UpdateMyProfileUseCase(
            UserRepositoryPort userRepositoryPort,
            UserProfileAuthorizationService authorizationService,
            BusinessContactSynchronizationPort contactSynchronizationPort
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.authorizationService = authorizationService;
        this.contactSynchronizationPort = contactSynchronizationPort;
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
        String correo = request.correo() == null
                ? currentUser.correo()
                : normalizeEmail(request.correo());

        boolean emailChanged = !Objects.equals(correo, currentUser.correo());
        boolean phoneChanged = !Objects.equals(telefono, currentUser.telefono());
        if (emailChanged && userRepositoryPort.existsByCorreoAndIdNot(correo, currentUser.id())) {
            throw new ConflictException("El correo ya esta registrado.");
        }

        User saved = userRepositoryPort.save(currentUser.updateProfile(nombre, correo, telefono));
        if (currentUser.rol() == UserRole.ENCARGADA && (emailChanged || phoneChanged)) {
            contactSynchronizationPort.synchronize(saved.correo(), saved.telefono(), saved.id());
        }
        return UserProfileResponse.from(saved);
    }

    private void validateRequest(UpdateMyProfileRequest request) {
        if (request == null) {
            throw new UserProfileValidationException("Los datos del perfil son obligatorios.");
        }
        if (request.rol() != null || request.password() != null) {
            throw new UserProfileValidationException("No se permite actualizar rol o password desde este endpoint.");
        }
        if (request.nombre() == null && request.telefono() == null && request.correo() == null) {
            throw new UserProfileValidationException("Debes enviar nombre, correo o telefono para actualizar el perfil.");
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

    private String normalizeEmail(String correo) {
        String normalized = correo.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new UserProfileValidationException("El correo no puede estar vacio.");
        }
        return normalized;
    }
}
