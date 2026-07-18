package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class OrderAuthorizationService {

    private final UserRepositoryPort userRepositoryPort;

    public OrderAuthorizationService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public User requireActiveUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.userId() == null) {
            throw new UnauthorizedException("Token de autenticacion requerido.");
        }

        User actor = userRepositoryPort.findById(authenticatedUser.userId())
                .orElseThrow(() -> new ForbiddenException("No tienes permisos para consultar pedidos."));

        if (!actor.activo()) {
            throw new ForbiddenException("No tienes permisos para consultar pedidos.");
        }

        return actor;
    }

    public User requireCliente(AuthenticatedUser authenticatedUser) {
        User actor = requireActiveUser(authenticatedUser);
        if (actor.rol() != UserRole.CLIENTE) {
            throw new ForbiddenException("Solo los clientes pueden crear pedidos.");
        }
        return actor;
    }

    public User requireClienteForHistory(AuthenticatedUser authenticatedUser) {
        User actor = requireActiveUser(authenticatedUser);
        if (actor.rol() != UserRole.CLIENTE) {
            throw new ForbiddenException("Solo los clientes pueden consultar su historial de pedidos.");
        }
        return actor;
    }

    public User requireEncargada(AuthenticatedUser authenticatedUser) {
        User actor = requireActiveUser(authenticatedUser);
        if (actor.rol() != UserRole.ENCARGADA) {
            throw new ForbiddenException("No tienes permisos para administrar pedidos.");
        }
        return actor;
    }
}
