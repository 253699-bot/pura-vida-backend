package com.puravida.modules.menu.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyMenuJpaRepository extends JpaRepository<DailyMenuEntity, Integer> {

    List<DailyMenuEntity> findByFechaOrderByIdAsc(LocalDate fecha);

    List<DailyMenuEntity> findByFechaAndPublicadoTrueOrderByIdAsc(LocalDate fecha);

    List<DailyMenuEntity> findByFechaAndDishIdAndPublicadoTrueOrderByIdAsc(LocalDate fecha, Integer dishId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE DailyMenuEntity item
            SET item.precioDia = :precioDia
            WHERE item.fecha = :fecha
              AND item.dishId = :dishId
              AND item.publicado = true
            """)
    int updatePrecioDiaByFechaAndDishIdAndPublicadoTrue(
            @Param("fecha") LocalDate fecha,
            @Param("dishId") Integer dishId,
            @Param("precioDia") BigDecimal precioDia
    );
}
