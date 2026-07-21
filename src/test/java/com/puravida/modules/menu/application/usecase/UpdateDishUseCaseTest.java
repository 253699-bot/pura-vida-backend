package com.puravida.modules.menu.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.CreateDishRequest;
import com.puravida.modules.menu.application.port.out.DailyMenuRepositoryPort;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.ConflictException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateDishUseCaseTest {

    @Mock
    private DishRepositoryPort dishRepositoryPort;

    @Mock
    private DailyMenuRepositoryPort dailyMenuRepositoryPort;

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

        ArgumentCaptor<com.puravida.modules.menu.domain.model.Dish> savedDish =
                ArgumentCaptor.forClass(com.puravida.modules.menu.domain.model.Dish.class);
        verify(dailyMenuRepositoryPort).updatePublishedDishForDate(any(LocalDate.class), savedDish.capture());
        assertThat(savedDish.getValue().precioBase()).isEqualByComparingTo("72.00");
    }

    @ParameterizedTest
    @ValueSource(strings = {"20.00", "80.00", "20.50", "79.99"})
    void keepsExactPriceWhenUpdatingDishAndTodayMenu(String price) {
        AuthenticatedUser actor = actor();
        var current = TestMenuData.dish();
        when(authorizationService.requireEncargada(actor)).thenReturn(TestMenuData.encargada());
        when(dishRepositoryPort.findById(current.id())).thenReturn(Optional.of(current));
        when(dishRepositoryPort.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.update(
                current.id(),
                new CreateDishRequest("Enchiladas", "Verdes", "platillo_fuerte", new BigDecimal(price)),
                actor
        );

        ArgumentCaptor<com.puravida.modules.menu.domain.model.Dish> savedDish =
                ArgumentCaptor.forClass(com.puravida.modules.menu.domain.model.Dish.class);
        verify(dailyMenuRepositoryPort).updatePublishedDishForDate(any(LocalDate.class), savedDish.capture());
        assertThat(response.precioBase()).isEqualByComparingTo(price);
        assertThat(savedDish.getValue().precioBase()).isEqualByComparingTo(price);
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

        verify(dailyMenuRepositoryPort, never()).updatePublishedDishForDate(any(), any());
    }

    private AuthenticatedUser actor() {
        return new AuthenticatedUser(2, "encargada@example.com", UserRole.ENCARGADA);
    }
}
