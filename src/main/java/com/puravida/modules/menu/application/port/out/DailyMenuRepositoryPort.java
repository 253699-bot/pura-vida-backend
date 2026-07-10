package com.puravida.modules.menu.application.port.out;

import com.puravida.modules.menu.domain.model.DailyMenuItem;
import com.puravida.modules.menu.domain.model.Dish;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMenuRepositoryPort {

    List<DailyMenuItem> findByFecha(LocalDate fecha);

    Optional<DailyMenuItem> findItemById(Integer menuItemId);

    List<DailyMenuItem> replaceForDate(LocalDate fecha, List<Dish> dishes, Integer creadoPor);

    DailyMenuItem updateAvailability(Integer menuItemId, boolean disponible);
}
