package com.puravida.modules.menu.application.port.out;

import com.puravida.modules.menu.domain.model.Dish;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DishRepositoryPort {

    List<Dish> findAllByIds(Collection<Integer> ids);

    Optional<Dish> findById(Integer id);

    Optional<Dish> findByIdForUpdate(Integer id);

    Dish save(Dish dish);

    List<Dish> findAllActive();
}