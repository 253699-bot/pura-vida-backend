package com.puravida.modules.menu.application.usecase;

import com.puravida.modules.menu.domain.model.DailyMenuItem;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.menu.domain.model.MenuAvailability;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

final class TestMenuData {

    private TestMenuData() {
    }

    static Dish dish() {
        return new Dish(
                10,
                "Tacos",
                "Orden de tacos",
                "platillo_fuerte",
                new BigDecimal("65.00"),
                null,
                true,
                LocalDateTime.now().minusDays(2),
                null
        );
    }

    static Dish inactiveDish() {
        Dish dish = dish();
        return new Dish(
                dish.id(),
                dish.nombre(),
                dish.descripcion(),
                dish.tipoPlatillo(),
                dish.precioBase(),
                dish.imagenKey(),
                false,
                dish.creadoEn(),
                dish.actualizadoEn()
        );
    }

    static DailyMenuItem menuItem() {
        return menuItem(LocalDate.now());
    }

    static DailyMenuItem unavailableMenuItem() {
        return new DailyMenuItem(
                5,
                LocalDate.now(),
                dish(),
                new BigDecimal("65.00"),
                2,
                LocalDateTime.now().minusHours(3),
                true,
                new MenuAvailability(3, 5, false, null, null, LocalDateTime.now())
        );
    }

    static DailyMenuItem menuItem(LocalDate fecha) {
        return new DailyMenuItem(
                5,
                fecha,
                dish(),
                new BigDecimal("65.00"),
                2,
                LocalDateTime.now().minusHours(3),
                true,
                new MenuAvailability(3, 5, true, null, null, null)
        );
    }

    static User encargada() {
        return new User(
                2,
                "Encargada",
                "encargada@example.com",
                null,
                "hash",
                UserRole.ENCARGADA,
                null,
                true,
                true,
                LocalDateTime.now().minusDays(1),
                null
        );
    }
}
