package com.puravida.modules.orders.infrastructure.repository;

import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.menu.domain.model.MenuAvailability;
import com.puravida.modules.menu.infrastructure.persistence.DailyMenuEntity;
import com.puravida.modules.menu.infrastructure.persistence.DailyMenuJpaRepository;
import com.puravida.modules.menu.infrastructure.persistence.DishEntity;
import com.puravida.modules.menu.infrastructure.persistence.DishJpaRepository;
import com.puravida.modules.menu.infrastructure.persistence.MenuAvailabilityEntity;
import com.puravida.modules.menu.infrastructure.persistence.MenuAvailabilityJpaRepository;
import com.puravida.modules.orders.application.port.out.MenuForOrderRepositoryPort;
import com.puravida.modules.orders.domain.model.OrderableMenuItem;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class MenuForOrderRepositoryAdapter implements MenuForOrderRepositoryPort {

    private final DailyMenuJpaRepository dailyMenuJpaRepository;
    private final DishJpaRepository dishJpaRepository;
    private final MenuAvailabilityJpaRepository availabilityJpaRepository;

    public MenuForOrderRepositoryAdapter(
            DailyMenuJpaRepository dailyMenuJpaRepository,
            DishJpaRepository dishJpaRepository,
            MenuAvailabilityJpaRepository availabilityJpaRepository
    ) {
        this.dailyMenuJpaRepository = dailyMenuJpaRepository;
        this.dishJpaRepository = dishJpaRepository;
        this.availabilityJpaRepository = availabilityJpaRepository;
    }

    @Override
    public List<OrderableMenuItem> findByFecha(LocalDate fecha) {
        List<DailyMenuEntity> menuItems = dailyMenuJpaRepository.findByFechaOrderByIdAsc(fecha);
        if (menuItems.isEmpty()) {
            return List.of();
        }

        Map<Integer, Dish> dishesById = dishJpaRepository.findAllById(dishIds(menuItems)).stream()
                .map(DishEntity::toDomain)
                .collect(Collectors.toMap(Dish::id, Function.identity()));
        Map<Integer, MenuAvailability> availabilityByMenuId = availabilityJpaRepository
                .findByMenuIdIn(menuIds(menuItems))
                .stream()
                .map(MenuAvailabilityEntity::toDomain)
                .collect(Collectors.toMap(MenuAvailability::menuItemId, Function.identity()));

        return menuItems.stream()
                .map(menuItem -> toOrderableItem(
                        menuItem,
                        dishesById.get(menuItem.dishId()),
                        availabilityByMenuId.get(menuItem.id())
                ))
                .toList();
    }

    private OrderableMenuItem toOrderableItem(
            DailyMenuEntity menuItem,
            Dish dish,
            MenuAvailability availability
    ) {
        return new OrderableMenuItem(
                menuItem.id(),
                menuItem.fecha(),
                menuItem.dishId(),
                dish.nombre(),
                menuItem.precioDia(),
                availability == null || availability.disponible(),
                dish.activo()
        );
    }

    private Collection<Integer> dishIds(List<DailyMenuEntity> menuItems) {
        return menuItems.stream().map(DailyMenuEntity::dishId).toList();
    }

    private Collection<Integer> menuIds(List<DailyMenuEntity> menuItems) {
        return menuItems.stream().map(DailyMenuEntity::id).toList();
    }
}
