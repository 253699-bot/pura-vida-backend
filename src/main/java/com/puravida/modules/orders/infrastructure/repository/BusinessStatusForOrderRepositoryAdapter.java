package com.puravida.modules.orders.infrastructure.repository;

import com.puravida.modules.business.infrastructure.persistence.BusinessDayStatusEntity;
import com.puravida.modules.business.infrastructure.persistence.BusinessDayStatusJpaRepository;
import com.puravida.modules.orders.application.port.out.BusinessStatusForOrderRepositoryPort;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class BusinessStatusForOrderRepositoryAdapter implements BusinessStatusForOrderRepositoryPort {

    private final BusinessDayStatusJpaRepository businessStatusJpaRepository;

    public BusinessStatusForOrderRepositoryAdapter(BusinessDayStatusJpaRepository businessStatusJpaRepository) {
        this.businessStatusJpaRepository = businessStatusJpaRepository;
    }

    @Override
    public Optional<Boolean> findOpenByFecha(LocalDate fecha) {
        return businessStatusJpaRepository.findByFecha(fecha)
                .map(BusinessDayStatusEntity::toDomain)
                .map(status -> status.abierto());
    }
}
