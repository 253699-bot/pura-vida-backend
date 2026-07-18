package com.puravida.modules.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.RegisterRequest;
import com.puravida.modules.auth.application.port.out.PasswordHasherPort;
import com.puravida.modules.auth.domain.exception.EmailAlreadyRegisteredException;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    @Test
    void registerAlwaysCreatesClientWithHashedPassword() {
        when(userRepositoryPort.existsByCorreo("ana@example.com")).thenReturn(false);
        when(passwordHasherPort.hash("password123")).thenReturn("hashed-password");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new User(
                    10,
                    user.nombre(),
                    user.correo(),
                    user.telefono(),
                    user.passwordHash(),
                    user.rol(),
                    user.iconoPerfil(),
                    user.notificacionesActivas(),
                    user.activo(),
                    user.creadoEn(),
                    user.actualizadoEn()
            );
        });

        registerUserUseCase.register(new RegisterRequest(
                " Ana Perez ",
                "ANA@EXAMPLE.COM",
                " 9610000000 ",
                "password123"
        ));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.rol()).isEqualTo(UserRole.CLIENTE);
        assertThat(savedUser.correo()).isEqualTo("ana@example.com");
        assertThat(savedUser.nombre()).isEqualTo("Ana Perez");
        assertThat(savedUser.telefono()).isEqualTo("9610000000");
        assertThat(savedUser.passwordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.activo()).isTrue();
        assertThat(savedUser.notificacionesActivas()).isTrue();
    }

    @Test
    void rejectsAlreadyRegisteredNormalizedEmail() {
        when(userRepositoryPort.existsByCorreo("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registerUserUseCase.register(new RegisterRequest(
                "Ana Perez",
                " ANA@EXAMPLE.COM ",
                null,
                "password123"
        )))
                .isInstanceOf(EmailAlreadyRegisteredException.class)
                .hasMessage("El correo ya esta registrado.");

        verify(userRepositoryPort, never()).save(any());
        verifyNoInteractions(passwordHasherPort);
    }
}
