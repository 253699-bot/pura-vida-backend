package com.puravida.modules.notifications.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class NotificationAuthorizationService {

    private final UserRepositoryPort userRepositoryPort;

    public NotificationAuthorizationService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public User requireActiveUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new UnauthorizedException("Token de autenticacion requerido.");
        }

        User user = userRepositoryPort.findById(authenticatedUser.userId())
                .orElseThrow(() -> new ForbiddenException("No tienes permisos para consultar notificaciones."));
        if (!user.activo()) {
            throw new ForbiddenException("No tienes permisos para consultar notificaciones.");
        }
        return user;
    }
}
