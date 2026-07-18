package com.puravida.modules.users.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class UserProfileAuthorizationService {

    private final UserRepositoryPort userRepositoryPort;

    public UserProfileAuthorizationService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public User requireActiveUser(AuthenticatedUser authenticatedUser) {
        Integer userId = requireUserId(authenticatedUser);
        return requireActive(userRepositoryPort.findById(userId)
                .orElseThrow(this::forbidden));
    }

    public User requireActiveUserForUpdate(AuthenticatedUser authenticatedUser) {
        Integer userId = requireUserId(authenticatedUser);
        return requireActive(userRepositoryPort.findByIdForUpdate(userId)
                .orElseThrow(this::forbidden));
    }

    private Integer requireUserId(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new UnauthorizedException("Token de autenticacion requerido.");
        }
        return authenticatedUser.userId();
    }

    private User requireActive(User user) {
        if (!user.activo()) {
            throw forbidden();
        }
        return user;
    }

    private ForbiddenException forbidden() {
        return new ForbiddenException("No tienes permisos para acceder a este perfil.");
    }
}
