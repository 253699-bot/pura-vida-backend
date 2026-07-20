package com.puravida.modules.businessconfiguration.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.businessconfiguration.application.dto.BusinessConfigurationResponse;
import com.puravida.modules.businessconfiguration.application.dto.UpdateBusinessConfigurationRequest;
import com.puravida.modules.businessconfiguration.application.port.in.UpdateBusinessConfigurationPort;
import com.puravida.modules.businessconfiguration.application.port.out.BusinessConfigurationRepositoryPort;
import com.puravida.modules.businessconfiguration.domain.exception.BusinessConfigurationValidationException;
import com.puravida.modules.businessconfiguration.domain.model.BusinessConfiguration;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.NotFoundException;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateBusinessConfigurationUseCase implements UpdateBusinessConfigurationPort {

    private final BusinessConfigurationRepositoryPort repositoryPort;
    private final BusinessConfigurationAuthorizationService authorizationService;

    public UpdateBusinessConfigurationUseCase(
            BusinessConfigurationRepositoryPort repositoryPort,
            BusinessConfigurationAuthorizationService authorizationService
    ) {
        this.repositoryPort = repositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public BusinessConfigurationResponse update(
            UpdateBusinessConfigurationRequest request,
            AuthenticatedUser authenticatedUser
    ) {
        User actor = authorizationService.requireEncargada(authenticatedUser);
        validateRequest(request);
        BusinessConfiguration current = repositoryPort.findSingletonForUpdate()
                .orElseThrow(() -> new NotFoundException("La configuracion del negocio no esta disponible."));
        BusinessConfiguration updated = current.updateDetails(
                request.nombreFonda() == null ? current.nombreFonda() : requiredText(request.nombreFonda(), "nombreFonda"),
                request.direccion() == null ? current.direccion() : optionalText(request.direccion()),
                request.horarios() == null ? current.horarios() : optionalText(request.horarios()),
                request.telefono() == null ? current.telefono() : optionalText(request.telefono()),
                request.correo() == null ? current.correo() : optionalEmail(request.correo()),
                actor.id()
        );
        return BusinessConfigurationResponse.from(repositoryPort.save(updated));
    }

    private void validateRequest(UpdateBusinessConfigurationRequest request) {
        if (request == null || (request.nombreFonda() == null && request.direccion() == null
                && request.horarios() == null && request.telefono() == null && request.correo() == null)) {
            throw new BusinessConfigurationValidationException("Debes enviar al menos un campo para actualizar.");
        }
    }

    private String requiredText(String value, String field) {
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessConfigurationValidationException(field + " no puede estar vacio.");
        }
        return normalized;
    }

    private String optionalText(String value) {
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String optionalEmail(String value) {
        String normalized = optionalText(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
