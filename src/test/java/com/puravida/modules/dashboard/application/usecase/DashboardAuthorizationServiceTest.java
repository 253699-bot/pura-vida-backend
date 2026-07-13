package com.puravida.modules.dashboard.application.usecase;

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
class DashboardAuthorizationServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private DashboardAuthorizationService service;

    @Test
    void rejectsMissingAuthenticatedUser() {
        assertThatThrownBy(() -> service.requireEncargada(null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void rejectsActiveClient() {
        when(userRepositoryPort.findById(1)).thenReturn(Optional.of(user(1, UserRole.CLIENTE, true)));

        assertThatThrownBy(() -> service.requireEncargada(
                new AuthenticatedUser(1, "cliente@example.com", UserRole.CLIENTE)
        )).isInstanceOf(ForbiddenException.class);
    }

    private User user(Integer id, UserRole role, boolean active) {
        return new User(
                id,
                "Usuario",
                "usuario@example.com",
                null,
                "hash",
                role,
                null,
                true,
                active,
                LocalDateTime.now(),
                null
        );
    }
}
