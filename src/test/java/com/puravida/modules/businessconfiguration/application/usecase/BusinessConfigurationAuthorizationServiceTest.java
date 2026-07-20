package com.puravida.modules.businessconfiguration.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessConfigurationAuthorizationServiceTest {

    @Mock UserRepositoryPort userRepositoryPort;
    @InjectMocks BusinessConfigurationAuthorizationService service;

    @Test
    void requiresTokenAndActiveEncargadaFromDatabase() {
        assertThatThrownBy(() -> service.requireEncargada(null))
                .isInstanceOf(UnauthorizedException.class);

        AuthenticatedUser clientToken = new AuthenticatedUser(7, "client@example.com", UserRole.CLIENTE);
        when(userRepositoryPort.findById(7)).thenReturn(Optional.of(user(7, UserRole.CLIENTE)));
        assertThatThrownBy(() -> service.requireEncargada(clientToken))
                .isInstanceOf(ForbiddenException.class);

        AuthenticatedUser adminToken = new AuthenticatedUser(4, "admin@example.com", UserRole.ENCARGADA);
        User manager = user(4, UserRole.ENCARGADA);
        when(userRepositoryPort.findById(4)).thenReturn(Optional.of(manager));
        assertThat(service.requireEncargada(adminToken)).isEqualTo(manager);
    }

    private User user(Integer id, UserRole role) {
        return new User(
                id, "Usuario", id + "@example.com", null, "hash", role,
                null, true, true, LocalDateTime.of(2026, 7, 1, 10, 0), null
        );
    }
}
