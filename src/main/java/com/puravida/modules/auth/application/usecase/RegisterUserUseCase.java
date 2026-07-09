package com.puravida.modules.auth.application.usecase;

import com.puravida.modules.auth.application.dto.RegisterRequest;
import com.puravida.modules.auth.application.dto.UserSummaryResponse;
import com.puravida.modules.auth.application.port.in.RegisterUserPort;
import com.puravida.modules.auth.application.port.out.PasswordHasherPort;
import com.puravida.modules.auth.domain.exception.EmailAlreadyRegisteredException;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserUseCase implements RegisterUserPort {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;

    public RegisterUserUseCase(UserRepositoryPort userRepositoryPort, PasswordHasherPort passwordHasherPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
    }

    @Override
    @Transactional
    public UserSummaryResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.correo());

        if (userRepositoryPort.existsByCorreo(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException();
        }

        User user = User.newClient(
                request.nombre().trim(),
                normalizedEmail,
                normalizeNullable(request.telefono()),
                passwordHasherPort.hash(request.password())
        );

        return UserSummaryResponse.from(userRepositoryPort.save(user));
    }

    private String normalizeEmail(String correo) {
        return correo.trim().toLowerCase();
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
