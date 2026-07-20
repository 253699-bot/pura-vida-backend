package com.puravida.modules.reports.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class WeeklyReportAuthorizationService {

    private final UserRepositoryPort userRepositoryPort;

    public WeeklyReportAuthorizationService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public User requireEncargada(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new UnauthorizedException("Token de autenticacion requerido.");
        }
        User actor = userRepositoryPort.findById(authenticatedUser.userId())
                .orElseThrow(this::forbidden);
        if (!actor.activo() || actor.rol() != UserRole.ENCARGADA) {
            throw forbidden();
        }
        return actor;
    }

    private ForbiddenException forbidden() {
        return new ForbiddenException("No tienes permisos para administrar reportes.");
    }
}
