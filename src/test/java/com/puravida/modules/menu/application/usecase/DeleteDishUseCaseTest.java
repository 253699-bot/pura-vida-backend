package com.puravida.modules.menu.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteDishUseCaseTest {

    @Mock
    private DishRepositoryPort dishRepositoryPort;

    @Mock
    private MenuAuthorizationService authorizationService;

    @InjectMocks
    private DeleteDishUseCase useCase;

    @Test
    void deactivatesExistingActiveDishWithoutPhysicalDeletion() {
        AuthenticatedUser encargada = encargada();
        Dish dish = TestMenuData.dish();
        when(authorizationService.requireEncargada(encargada)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findById(dish.id())).thenReturn(Optional.of(dish));
        when(dishRepositoryPort.save(any(Dish.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.delete(dish.id(), encargada);

        ArgumentCaptor<Dish> dishCaptor = ArgumentCaptor.forClass(Dish.class);
        verify(dishRepositoryPort).save(dishCaptor.capture());
        assertThat(dishCaptor.getValue().id()).isEqualTo(dish.id());
        assertThat(dishCaptor.getValue().activo()).isFalse();
        assertThat(response.activo()).isFalse();
    }

    @Test
    void rejectsMissingDish() {
        AuthenticatedUser encargada = encargada();
        when(authorizationService.requireEncargada(encargada)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.delete(99, encargada))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void rejectsDishAlreadyInactive() {
        AuthenticatedUser encargada = encargada();
        Dish inactiveDish = TestMenuData.inactiveDish();
        when(authorizationService.requireEncargada(encargada)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findById(inactiveDish.id())).thenReturn(Optional.of(inactiveDish));

        assertThatThrownBy(() -> useCase.delete(inactiveDish.id(), encargada))
                .isInstanceOf(ConflictException.class);
    }

    private AuthenticatedUser encargada() {
        return new AuthenticatedUser(2, "encargada@example.com", UserRole.ENCARGADA);
    }
}
