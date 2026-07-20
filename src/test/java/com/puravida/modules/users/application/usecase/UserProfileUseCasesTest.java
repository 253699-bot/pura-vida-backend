package com.puravida.modules.users.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.users.application.dto.UpdateMyProfileRequest;
import com.puravida.modules.users.application.dto.UserProfileResponse;
import com.puravida.modules.users.application.port.out.BusinessContactSynchronizationPort;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.exception.UserProfileValidationException;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.ForbiddenException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserProfileUseCasesTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private BusinessContactSynchronizationPort contactSynchronizationPort;

    private GetMyProfileUseCase getUseCase;
    private UpdateMyProfileUseCase updateUseCase;

    @BeforeEach
    void setUp() {
        UserProfileAuthorizationService authorizationService =
                new UserProfileAuthorizationService(userRepositoryPort);
        getUseCase = new GetMyProfileUseCase(authorizationService);
        updateUseCase = new UpdateMyProfileUseCase(
                userRepositoryPort,
                authorizationService,
                contactSynchronizationPort
        );
    }

    @Test
    void getsOnlyUserIdentifiedByJwt() {
        when(userRepositoryPort.findById(7)).thenReturn(Optional.of(activeClient()));

        UserProfileResponse response = getUseCase.get(authenticatedClient());

        assertThat(response.id()).isEqualTo(7);
        assertThat(response.correo()).isEqualTo("ana@example.com");
        assertThat(response.rol()).isEqualTo("cliente");
        verify(userRepositoryPort).findById(7);
    }

    @Test
    void updatesOwnNameAndPhoneWhilePreservingProtectedData() {
        User current = activeClient();
        when(userRepositoryPort.findByIdForUpdate(7)).thenReturn(Optional.of(current));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = updateUseCase.update(
                new UpdateMyProfileRequest(" Ana Maria ", " 9611111111 ", null, null, null),
                authenticatedClient()
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.id()).isEqualTo(7);
        assertThat(saved.nombre()).isEqualTo("Ana Maria");
        assertThat(saved.telefono()).isEqualTo("9611111111");
        assertThat(saved.correo()).isEqualTo(current.correo());
        assertThat(saved.rol()).isEqualTo(UserRole.CLIENTE);
        assertThat(saved.passwordHash()).isEqualTo("bcrypt-hash");
        assertThat(saved.actualizadoEn()).isNotNull();
        assertThat(response.rol()).isEqualTo("cliente");
    }

    @Test
    void rejectsRoleOrPasswordChanges() {
        when(userRepositoryPort.findByIdForUpdate(7)).thenReturn(Optional.of(activeClient()));

        assertThatThrownBy(() -> updateUseCase.update(
                new UpdateMyProfileRequest(
                        "Ana Maria",
                        null,
                        null,
                        "encargada",
                        "new-password"
                ),
                authenticatedClient()
        ))
                .isInstanceOf(UserProfileValidationException.class)
                .hasMessage("No se permite actualizar rol o password desde este endpoint.");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void allowsClearingOwnPhone() {
        when(userRepositoryPort.findByIdForUpdate(7)).thenReturn(Optional.of(activeClient()));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = updateUseCase.update(
                new UpdateMyProfileRequest(null, "  ", null, null, null),
                authenticatedClient()
        );

        assertThat(response.telefono()).isNull();
    }

    @Test
    void rejectsEmptyUpdate() {
        when(userRepositoryPort.findByIdForUpdate(7)).thenReturn(Optional.of(activeClient()));

        assertThatThrownBy(() -> updateUseCase.update(
                new UpdateMyProfileRequest(null, null, null, null, null),
                authenticatedClient()
        ))
                .isInstanceOf(UserProfileValidationException.class)
                .hasMessage("Debes enviar nombre, correo o telefono para actualizar el perfil.");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void normalizesAndUpdatesClientsEmailWithoutSynchronizingPublicContact() {
        when(userRepositoryPort.findByIdForUpdate(7)).thenReturn(Optional.of(activeClient()));
        when(userRepositoryPort.existsByCorreoAndIdNot("ana.nueva@example.com", 7)).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = updateUseCase.update(
                new UpdateMyProfileRequest(null, null, " ANA.NUEVA@EXAMPLE.COM ", null, null),
                authenticatedClient()
        );

        assertThat(response.correo()).isEqualTo("ana.nueva@example.com");
        verify(contactSynchronizationPort, never()).synchronize(any(), any(), any());
    }

    @Test
    void rejectsEmailOwnedByAnotherUser() {
        when(userRepositoryPort.findByIdForUpdate(7)).thenReturn(Optional.of(activeClient()));
        when(userRepositoryPort.existsByCorreoAndIdNot("ocupado@example.com", 7)).thenReturn(true);

        assertThatThrownBy(() -> updateUseCase.update(
                new UpdateMyProfileRequest(null, null, "ocupado@example.com", null, null),
                authenticatedClient()
        ))
                .isInstanceOf(ConflictException.class)
                .hasMessage("El correo ya esta registrado.");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void synchronizesEncargadasChangedContactInsideProfileUpdate() {
        User current = activeEncargada();
        AuthenticatedUser authenticated = new AuthenticatedUser(4, current.correo(), UserRole.ENCARGADA);
        when(userRepositoryPort.findByIdForUpdate(4)).thenReturn(Optional.of(current));
        when(userRepositoryPort.existsByCorreoAndIdNot("admin.nueva@example.com", 4)).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        updateUseCase.update(
                new UpdateMyProfileRequest(null, "9619999999", "ADMIN.NUEVA@EXAMPLE.COM", null, null),
                authenticated
        );

        verify(contactSynchronizationPort).synchronize(
                "admin.nueva@example.com", "9619999999", 4
        );
    }

    @Test
    void rejectsInactiveAuthenticatedUser() {
        User inactive = new User(
                7,
                "Ana Perez",
                "ana@example.com",
                null,
                "bcrypt-hash",
                UserRole.CLIENTE,
                null,
                true,
                false,
                LocalDateTime.of(2026, 7, 1, 10, 0),
                null
        );
        when(userRepositoryPort.findById(7)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> getUseCase.get(authenticatedClient()))
                .isInstanceOf(ForbiddenException.class);
    }

    private AuthenticatedUser authenticatedClient() {
        return new AuthenticatedUser(7, "ana@example.com", UserRole.CLIENTE);
    }

    private User activeClient() {
        return new User(
                7,
                "Ana Perez",
                "ana@example.com",
                "9610000000",
                "bcrypt-hash",
                UserRole.CLIENTE,
                null,
                true,
                true,
                LocalDateTime.of(2026, 7, 1, 10, 0),
                null
        );
    }

    private User activeEncargada() {
        return new User(
                4,
                "Encargada",
                "admin@example.com",
                "9610000000",
                "bcrypt-hash",
                UserRole.ENCARGADA,
                null,
                true,
                true,
                LocalDateTime.of(2026, 7, 1, 10, 0),
                null
        );
    }
}
