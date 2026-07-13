package com.puravida.modules.menu.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.menu.application.dto.CreateDishRequest;
import com.puravida.modules.menu.application.dto.DishResponse;
import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateDishUseCaseTest {

    @Mock
    private DishRepositoryPort dishRepositoryPort;

    @Mock
    private MenuAuthorizationService authorizationService;

    @InjectMocks
    private CreateDishUseCase createDishUseCase;

    @Test
    void createsActiveDishWithGeneratedId() {
        AuthenticatedUser encargada = new AuthenticatedUser(2, "encargada@example.com", UserRole.ENCARGADA);
        when(authorizationService.requireEncargada(encargada)).thenReturn(encargadaUser());
        when(dishRepositoryPort.save(any(Dish.class))).thenAnswer(invocation -> {
            Dish dish = invocation.getArgument(0);
            return new Dish(
                    12,
                    dish.nombre(),
                    dish.descripcion(),
                    dish.tipoPlatillo(),
                    dish.precioBase(),
                    dish.activo(),
                    LocalDateTime.of(2026, 7, 12, 10, 0),
                    null
            );
        });

        DishResponse response = createDishUseCase.create(
                new CreateDishRequest(
                        "  Comida corrida  ",
                        "  Incluye sopa.  ",
                        "platillo_fuerte",
                        new BigDecimal("85.00")
                ),
                encargada
        );

        ArgumentCaptor<Dish> dishCaptor = ArgumentCaptor.forClass(Dish.class);
        verify(dishRepositoryPort).save(dishCaptor.capture());
        assertEquals(12, response.id());
        assertTrue(response.activo());
        assertEquals("Comida corrida", dishCaptor.getValue().nombre());
        assertEquals("Incluye sopa.", dishCaptor.getValue().descripcion());
    }

    private User encargadaUser() {
        return new User(
                2,
                "Encargada",
                "encargada@example.com",
                null,
                null,
                UserRole.ENCARGADA,
                null,
                true,
                true,
                LocalDateTime.of(2026, 7, 12, 9, 0),
                null
        );
    }
}
