package com.puravida.modules.menu.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyMenuJpaRepository extends JpaRepository<DailyMenuEntity, Integer> {

    List<DailyMenuEntity> findByFechaOrderByIdAsc(LocalDate fecha);

    void deleteByIdIn(Collection<Integer> ids);
}
