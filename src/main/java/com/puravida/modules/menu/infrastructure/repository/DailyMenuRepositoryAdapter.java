package com.puravida.modules.menu.infrastructure.repository;

import com.puravida.modules.menu.application.port.out.DailyMenuRepositoryPort;
import com.puravida.modules.menu.domain.model.DailyMenuItem;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.menu.domain.model.MenuAvailability;
import com.puravida.modules.menu.infrastructure.persistence.DailyMenuEntity;
import com.puravida.modules.menu.infrastructure.persistence.DailyMenuJpaRepository;
import com.puravida.modules.menu.infrastructure.persistence.DishEntity;
import com.puravida.modules.menu.infrastructure.persistence.DishJpaRepository;
import com.puravida.modules.menu.infrastructure.persistence.MenuAvailabilityEntity;
import com.puravida.modules.menu.infrastructure.persistence.MenuAvailabilityJpaRepository;
import com.puravida.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class DailyMenuRepositoryAdapter implements DailyMenuRepositoryPort {

    private final DailyMenuJpaRepository dailyMenuJpaRepository;
    private final DishJpaRepository dishJpaRepository;
    private final MenuAvailabilityJpaRepository availabilityJpaRepository;

    public DailyMenuRepositoryAdapter(
            DailyMenuJpaRepository dailyMenuJpaRepository,
            DishJpaRepository dishJpaRepository,
            MenuAvailabilityJpaRepository availabilityJpaRepository
    ) {
        this.dailyMenuJpaRepository = dailyMenuJpaRepository;
        this.dishJpaRepository = dishJpaRepository;
        this.availabilityJpaRepository = availabilityJpaRepository;
    }

    @Override
    public List<DailyMenuItem> findByFecha(LocalDate fecha) {
        return toDomain(dailyMenuJpaRepository.findByFechaOrderByIdAsc(fecha));
    }

    @Override
    public Optional<DailyMenuItem> findItemById(Integer menuItemId) {
        return dailyMenuJpaRepository.findById(menuItemId)
                .map(entity -> toDomain(List.of(entity)).get(0));
    }

    @Override
    public List<DailyMenuItem> replaceForDate(LocalDate fecha, List<Dish> dishes, Integer creadoPor) {
        List<DailyMenuEntity> existingItems = dailyMenuJpaRepository.findByFechaOrderByIdAsc(fecha);
        Set<Integer> requestedDishIds = dishes.stream().map(Dish::id).collect(Collectors.toSet());
        List<Integer> idsToRemove = existingItems.stream()
                .filter(item -> !requestedDishIds.contains(item.dishId()))
                .map(DailyMenuEntity::id)
                .toList();

        if (!idsToRemove.isEmpty()) {
            availabilityJpaRepository.deleteByMenuIdIn(idsToRemove);
            dailyMenuJpaRepository.deleteByIdIn(idsToRemove);
            dailyMenuJpaRepository.flush();
        }

        Map<Integer, DailyMenuEntity> currentByDishId = dailyMenuJpaRepository.findByFechaOrderByIdAsc(fecha).stream()
                .collect(Collectors.toMap(DailyMenuEntity::dishId, Function.identity()));

        for (Dish dish : dishes) {
            if (!currentByDishId.containsKey(dish.id())) {
                DailyMenuEntity savedItem = dailyMenuJpaRepository.save(DailyMenuEntity.newItem(fecha, dish, creadoPor));
                availabilityJpaRepository.save(MenuAvailabilityEntity.available(savedItem.id()));
            }
        }

        return findByFecha(fecha);
    }

    @Override
    public DailyMenuItem updateAvailability(Integer menuItemId, boolean disponible) {
        MenuAvailabilityEntity availability = availabilityJpaRepository.findByMenuId(menuItemId)
                .orElseGet(() -> MenuAvailabilityEntity.available(menuItemId));
        availability.changeAvailability(disponible);
        availabilityJpaRepository.save(availability);

        return findItemById(menuItemId)
                .orElseThrow(() -> new NotFoundException("No se encontro el item del menu."));
    }

    private List<DailyMenuItem> toDomain(List<DailyMenuEntity> menuItems) {
        if (menuItems.isEmpty()) {
            return List.of();
        }

        Map<Integer, Dish> dishesById = dishJpaRepository.findAllById(dishIds(menuItems)).stream()
                .map(DishEntity::toDomain)
                .collect(Collectors.toMap(Dish::id, Function.identity()));
        Map<Integer, MenuAvailability> availabilityByMenuId = availabilityJpaRepository.findByMenuIdIn(menuIds(menuItems))
                .stream()
                .map(MenuAvailabilityEntity::toDomain)
                .collect(Collectors.toMap(MenuAvailability::menuItemId, Function.identity()));

        return menuItems.stream()
                .map(item -> item.toDomain(dishesById.get(item.dishId()), availabilityByMenuId.get(item.id())))
                .toList();
    }

    private Collection<Integer> dishIds(List<DailyMenuEntity> menuItems) {
        return menuItems.stream().map(DailyMenuEntity::dishId).toList();
    }

    private Collection<Integer> menuIds(List<DailyMenuEntity> menuItems) {
        return menuItems.stream().map(DailyMenuEntity::id).toList();
    }
}
