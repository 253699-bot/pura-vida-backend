package com.puravida.modules.auth.application.usecase;

import com.puravida.modules.auth.application.dto.AuthResponse;
import com.puravida.modules.auth.application.dto.LoginRequest;
import com.puravida.modules.auth.application.dto.UserSummaryResponse;
import com.puravida.modules.auth.application.port.in.LoginUserPort;
import com.puravida.modules.auth.application.port.out.JwtTokenPort;
import com.puravida.modules.auth.application.port.out.PasswordHasherPort;
import com.puravida.modules.auth.domain.exception.InactiveUserException;
import com.puravida.modules.auth.domain.exception.InvalidCredentialsException;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUserUseCase implements LoginUserPort {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final JwtTokenPort jwtTokenPort;

    public LoginUserUseCase(
            UserRepositoryPort userRepositoryPort,
            PasswordHasherPort passwordHasherPort,
            JwtTokenPort jwtTokenPort
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.jwtTokenPort = jwtTokenPort;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.correo().trim().toLowerCase();
        User user = userRepositoryPort.findByCorreo(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.activo()) {
            throw new InactiveUserException();
        }

        if (!passwordHasherPort.matches(request.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        return AuthResponse.bearer(
                jwtTokenPort.generate(user),
                jwtTokenPort.expirationMinutes(),
                UserSummaryResponse.from(user)
        );
    }
}
