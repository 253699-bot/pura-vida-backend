package com.puravida.modules.reports.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ForbiddenException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeeklyReportAuthorizationServiceTest {

    @Mock UserRepositoryPort userRepositoryPort;
    @InjectMocks WeeklyReportAuthorizationService service;

    @Test
    void rejectsClientEvenWhenJwtClaimsAValidAccount() {
        AuthenticatedUser token = new AuthenticatedUser(7, "client@example.com", UserRole.CLIENTE);
        User client = new User(
                7, "Cliente", "client@example.com", null, "hash", UserRole.CLIENTE,
                null, true, true, LocalDateTime.of(2026, 7, 1, 10, 0), null
        );
        when(userRepositoryPort.findById(7)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.requireEncargada(token))
                .isInstanceOf(ForbiddenException.class);
    }
}
