package com.puravida.modules.menu.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DishJpaRepository extends JpaRepository<DishEntity, Integer> {

    List<DishEntity> findByActivoTrueOrderByNombreAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT dish FROM DishEntity dish WHERE dish.id = :id")
    Optional<DishEntity> findByIdForUpdate(@Param("id") Integer id);
}