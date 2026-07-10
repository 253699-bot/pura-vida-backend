package com.puravida.modules.menu.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuAvailabilityJpaRepository extends JpaRepository<MenuAvailabilityEntity, Integer> {

    List<MenuAvailabilityEntity> findByMenuIdIn(Collection<Integer> menuIds);

    Optional<MenuAvailabilityEntity> findByMenuId(Integer menuId);

    void deleteByMenuIdIn(Collection<Integer> menuIds);
}
