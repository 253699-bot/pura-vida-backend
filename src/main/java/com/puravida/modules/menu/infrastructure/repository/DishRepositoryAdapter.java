package com.puravida.modules.menu.infrastructure.repository;

import com.puravida.modules.menu.application.port.out.DishRepositoryPort;
import com.puravida.modules.menu.domain.model.Dish;
import com.puravida.modules.menu.infrastructure.persistence.DishEntity;
import com.puravida.modules.menu.infrastructure.persistence.DishJpaRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class DishRepositoryAdapter implements DishRepositoryPort {

    private final DishJpaRepository dishJpaRepository;

    public DishRepositoryAdapter(DishJpaRepository dishJpaRepository) {
        this.dishJpaRepository = dishJpaRepository;
    }

    @Override
    public List<Dish> findAllByIds(Collection<Integer> ids) {
        return dishJpaRepository.findAllById(ids).stream()
                .map(DishEntity::toDomain)
                .toList();
    }
}
