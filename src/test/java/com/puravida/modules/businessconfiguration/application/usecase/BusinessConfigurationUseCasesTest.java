package com.puravida.modules.businessconfiguration.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.businessconfiguration.application.dto.StoredBusinessLogo;
import com.puravida.modules.businessconfiguration.application.dto.UpdateBusinessConfigurationRequest;
import com.puravida.modules.businessconfiguration.application.port.out.BusinessConfigurationRepositoryPort;
import com.puravida.modules.businessconfiguration.application.port.out.BusinessLogoStoragePort;
import com.puravida.modules.businessconfiguration.domain.model.BusinessConfiguration;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessConfigurationUseCasesTest {

    @Mock
    private BusinessConfigurationRepositoryPort repositoryPort;
    @Mock
    private BusinessLogoStoragePort storagePort;
    @Mock
    private BusinessConfigurationAuthorizationService authorizationService;

    private UpdateBusinessConfigurationUseCase updateUseCase;
    private UpdateBusinessLogoUseCase logoUseCase;

    @BeforeEach
    void setUp() {
        updateUseCase = new UpdateBusinessConfigurationUseCase(repositoryPort, authorizationService);
        logoUseCase = new UpdateBusinessLogoUseCase(repositoryPort, storagePort, authorizationService);
    }

    @Test
    void partiallyUpdatesSingletonWithEncargadaAsActor() {
        when(authorizationService.requireEncargada(authenticated())).thenReturn(encargada());
        when(repositoryPort.findSingletonForUpdate()).thenReturn(Optional.of(configuration()));
        when(repositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = updateUseCase.update(
                new UpdateBusinessConfigurationRequest(null, " Calle Central ", null, null, " INFO@EXAMPLE.COM "),
                authenticated()
        );

        ArgumentCaptor<BusinessConfiguration> captor = ArgumentCaptor.forClass(BusinessConfiguration.class);
        verify(repositoryPort).save(captor.capture());
        assertThat(captor.getValue().nombreFonda()).isEqualTo("PuraVida");
        assertThat(captor.getValue().direccion()).isEqualTo("Calle Central");
        assertThat(captor.getValue().correo()).isEqualTo("info@example.com");
        assertThat(captor.getValue().actualizadoPor()).isEqualTo(4);
        assertThat(response.logoUrl()).isNull();
    }

    @Test
    void removesNewLogoWhenDatabaseSaveFailsWithoutTransactionSynchronization() {
        byte[] content = new byte[]{1, 2, 3};
        when(authorizationService.requireEncargada(authenticated())).thenReturn(encargada());
        when(repositoryPort.findSingletonForUpdate()).thenReturn(Optional.of(configuration()));
        when(storagePort.store(content, "image/png"))
                .thenReturn(new StoredBusinessLogo("new-key.png", "image/png", content.length));
        when(repositoryPort.save(any())).thenThrow(new IllegalStateException("database failure"));

        assertThatThrownBy(() -> logoUseCase.update(content, "image/png", authenticated()))
                .isInstanceOf(IllegalStateException.class);

        verify(storagePort).delete("new-key.png");
    }

    private AuthenticatedUser authenticated() {
        return new AuthenticatedUser(4, "admin@example.com", UserRole.ENCARGADA);
    }

    private User encargada() {
        return new User(
                4, "Encargada", "admin@example.com", "9610000000", "hash",
                UserRole.ENCARGADA, null, true, true,
                LocalDateTime.of(2026, 7, 1, 10, 0), null
        );
    }

    private BusinessConfiguration configuration() {
        return new BusinessConfiguration(
                1, "PuraVida", null, null, null, "9610000000",
                "admin@example.com", 4, LocalDateTime.of(2026, 7, 1, 10, 0)
        );
    }
}
