package com.puravida.modules.menu.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.CreateDishRequest;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateDishUseCaseTest {

    @Mock
    private DishRepositoryPort dishRepositoryPort;

    @Mock
    private MenuAuthorizationService authorizationService;

    @InjectMocks
    private UpdateDishUseCase useCase;

    @Test
    void updatesOnlySupportedDishFieldsAndKeepsItActive() {
        AuthenticatedUser actor = actor();
        var current = TestMenuData.dish();
        when(authorizationService.requireEncargada(actor)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findById(current.id())).thenReturn(Optional.of(current));
        when(dishRepositoryPort.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.update(
                current.id(),
                new CreateDishRequest("Enchiladas", "  Verdes  ", "platillo_fuerte", new BigDecimal("72.00")),
                actor
        );

        assertThat(response.nombre()).isEqualTo("Enchiladas");
        assertThat(response.descripcion()).isEqualTo("Verdes");
        assertThat(response.precioBase()).isEqualByComparingTo("72.00");
        assertThat(response.activo()).isTrue();
        assertThat(response.actualizadoEn()).isNotNull();
    }

    @Test
    void rejectsEditingRetiredDish() {
        AuthenticatedUser actor = actor();
        when(authorizationService.requireEncargada(actor)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findById(10)).thenReturn(Optional.of(TestMenuData.inactiveDish()));

        assertThatThrownBy(() -> useCase.update(
                10,
                new CreateDishRequest("Tacos", null, "platillo_fuerte", BigDecimal.TEN),
                actor
        )).isInstanceOf(ConflictException.class);
    }

    private AuthenticatedUser actor() {
        return new AuthenticatedUser(2, "encargada@example.com", UserRole.ENCARGADA);
    }
}
