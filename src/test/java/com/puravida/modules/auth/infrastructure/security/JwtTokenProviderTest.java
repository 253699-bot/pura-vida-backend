package com.puravida.modules.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    @Test
    void readsGeneratedTokenClaims() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
                "12345678901234567890123456789012",
                120
        );
        User user = new User(
                5,
                "Encargada",
                "encargada@example.com",
                null,
                "hash",
                UserRole.ENCARGADA,
                null,
                true,
                true,
                LocalDateTime.now().minusDays(1),
                null
        );

        AuthenticatedUser authenticatedUser = jwtTokenProvider.read(jwtTokenProvider.generate(user));

        assertThat(authenticatedUser.userId()).isEqualTo(5);
        assertThat(authenticatedUser.correo()).isEqualTo("encargada@example.com");
        assertThat(authenticatedUser.rol()).isEqualTo(UserRole.ENCARGADA);
    }
}
