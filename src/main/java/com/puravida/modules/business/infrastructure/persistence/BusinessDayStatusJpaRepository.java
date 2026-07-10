package com.puravida.modules.business.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessDayStatusJpaRepository extends JpaRepository<BusinessDayStatusEntity, Integer> {

    Optional<BusinessDayStatusEntity> findByFecha(LocalDate fecha);
}
