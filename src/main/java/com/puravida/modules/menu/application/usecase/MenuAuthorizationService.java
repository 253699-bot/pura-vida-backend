package com.puravida.modules.menu.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class MenuAuthorizationService {

    private final UserRepositoryPort userRepositoryPort;

    public MenuAuthorizationService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public User requireEncargada(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new UnauthorizedException("Token de autenticacion requerido.");
        }

        User actor = userRepositoryPort.findById(authenticatedUser.userId())
                .orElseThrow(() -> new ForbiddenException("No tienes permisos para administrar el menu."));

        if (!actor.activo() || actor.rol() != UserRole.ENCARGADA) {
            throw new ForbiddenException("No tienes permisos para administrar el menu.");
        }

        return actor;
    }
}
