package com.puravida.modules.menu.application.port.out;

import com.puravida.modules.menu.domain.model.Dish;
import java.util.Collection;
import java.util.List;

public interface DishRepositoryPort {

    List<Dish> findAllByIds(Collection<Integer> ids);
}
