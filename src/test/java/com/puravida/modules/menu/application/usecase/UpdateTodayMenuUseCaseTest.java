package com.puravida.modules.menu.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.UpdateTodayMenuItemRequest;
import com.puravida.modules.menu.application.dto.UpdateTodayMenuRequest;
import com.puravida.modules.menu.application.port.out.DailyMenuRepositoryPort;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.menu.domain.exception.MenuValidationException;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTodayMenuUseCaseTest {

    @Mock
    private DailyMenuRepositoryPort dailyMenuRepositoryPort;

    @Mock
    private DishRepositoryPort dishRepositoryPort;

    @Mock
    private MenuAuthorizationService authorizationService;

    @InjectMocks
    private UpdateTodayMenuUseCase useCase;

    @Test
    void rejectsDuplicatedDishes() {
        AuthenticatedUser user = authenticatedEncargada();
        when(authorizationService.requireEncargada(user)).thenReturn(TestMenuData.encargada());

        assertThatThrownBy(() -> useCase.updateToday(
                new UpdateTodayMenuRequest(List.of(
                        new UpdateTodayMenuItemRequest(10),
                        new UpdateTodayMenuItemRequest(10)
                )),
                user
        )).isInstanceOf(MenuValidationException.class);

        verify(dishRepositoryPort, never()).findAllByIds(any());
        verify(dailyMenuRepositoryPort, never()).replaceForDate(any(), any(), any());
    }

    @Test
    void rejectsMissingDish() {
        AuthenticatedUser user = authenticatedEncargada();
        when(authorizationService.requireEncargada(user)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findAllByIds(List.of(99))).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.updateToday(
                new UpdateTodayMenuRequest(List.of(new UpdateTodayMenuItemRequest(99))),
                user
        )).isInstanceOf(NotFoundException.class);
    }

    @Test
    void rejectsInactiveDish() {
        AuthenticatedUser user = authenticatedEncargada();
        when(authorizationService.requireEncargada(user)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findAllByIds(List.of(10))).thenReturn(List.of(TestMenuData.inactiveDish()));

        assertThatThrownBy(() -> useCase.updateToday(
                new UpdateTodayMenuRequest(List.of(new UpdateTodayMenuItemRequest(10))),
                user
        )).isInstanceOf(MenuValidationException.class);
    }

    @Test
    void replacesTodayMenuWithExistingActiveDishes() {
        AuthenticatedUser user = authenticatedEncargada();
        Dish dish = TestMenuData.dish();
        when(authorizationService.requireEncargada(user)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findAllByIds(List.of(10))).thenReturn(List.of(dish));
        when(dailyMenuRepositoryPort.replaceForDate(any(LocalDate.class), eq(List.of(dish)), eq(2)))
                .thenReturn(List.of(TestMenuData.menuItem()));

        var response = useCase.updateToday(
                new UpdateTodayMenuRequest(List.of(new UpdateTodayMenuItemRequest(10))),
                user
        );

        assertThat(response.configured()).isTrue();
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).id()).isEqualTo(5);
    }

    @Test
    void allowsRetiringEveryPublishedItemWithoutDeletingHistory() {
        AuthenticatedUser user = authenticatedEncargada();
        when(authorizationService.requireEncargada(user)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findAllByIds(List.of())).thenReturn(List.of());
        when(dailyMenuRepositoryPort.replaceForDate(any(LocalDate.class), eq(List.of()), eq(2)))
                .thenReturn(List.of());

        var response = useCase.updateToday(new UpdateTodayMenuRequest(List.of()), user);

        assertThat(response.configured()).isFalse();
        assertThat(response.items()).isEmpty();
        verify(dailyMenuRepositoryPort).replaceForDate(any(LocalDate.class), eq(List.of()), eq(2));
    }

    private AuthenticatedUser authenticatedEncargada() {
        return new AuthenticatedUser(2, "encargada@example.com", UserRole.ENCARGADA);
    }
}
