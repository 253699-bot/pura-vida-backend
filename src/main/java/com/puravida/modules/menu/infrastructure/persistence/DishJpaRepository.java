package com.puravida.modules.menu.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DishJpaRepository extends JpaRepository<DishEntity, Integer> {
}
