package com.puravida.modules.menu.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.UpdateMenuItemAvailabilityRequest;
import com.puravida.modules.menu.application.port.out.DailyMenuRepositoryPort;
import com.puravida.modules.menu.domain.exception.MenuValidationException;
import com.puravida.modules.menu.domain.model.DailyMenuItem;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTodayMenuItemAvailabilityUseCaseTest {

    @Mock
    private DailyMenuRepositoryPort dailyMenuRepositoryPort;

    @Mock
    private MenuAuthorizationService authorizationService;

    @InjectMocks
    private UpdateTodayMenuItemAvailabilityUseCase useCase;

    @Test
    void rejectsNullAvailability() {
        AuthenticatedUser user = authenticatedEncargada();
        when(authorizationService.requireEncargada(user)).thenReturn(TestMenuData.encargada());

        assertThatThrownBy(() -> useCase.updateAvailability(
                5,
                new UpdateMenuItemAvailabilityRequest(null),
                user
        )).isInstanceOf(MenuValidationException.class);
    }

    @Test
    void rejectsItemsOutsideTodayMenu() {
        AuthenticatedUser user = authenticatedEncargada();
        DailyMenuItem yesterdayItem = TestMenuData.menuItem(LocalDate.now().minusDays(1));
        when(authorizationService.requireEncargada(user)).thenReturn(TestMenuData.encargada());
        when(dailyMenuRepositoryPort.findItemById(5)).thenReturn(Optional.of(yesterdayItem));

        assertThatThrownBy(() -> useCase.updateAvailability(
                5,
                new UpdateMenuItemAvailabilityRequest(false),
                user
        )).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updatesAvailabilityForTodayMenuItem() {
        AuthenticatedUser user = authenticatedEncargada();
        when(authorizationService.requireEncargada(user)).thenReturn(TestMenuData.encargada());
        when(dailyMenuRepositoryPort.findItemById(5)).thenReturn(Optional.of(TestMenuData.menuItem()));
        when(dailyMenuRepositoryPort.updateAvailability(5, false)).thenReturn(TestMenuData.unavailableMenuItem());

        var response = useCase.updateAvailability(
                5,
                new UpdateMenuItemAvailabilityRequest(false),
                user
        );

        assertThat(response.id()).isEqualTo(5);
        assertThat(response.disponible()).isFalse();
        verify(dailyMenuRepositoryPort).updateAvailability(5, false);
    }

    private AuthenticatedUser authenticatedEncargada() {
        return new AuthenticatedUser(2, "encargada@example.com", UserRole.ENCARGADA);
    }
}
