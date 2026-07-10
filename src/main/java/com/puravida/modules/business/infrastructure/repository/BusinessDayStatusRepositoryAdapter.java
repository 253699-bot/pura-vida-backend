package com.puravida.modules.business.infrastructure.repository;

import com.puravida.modules.business.application.port.out.BusinessDayStatusRepositoryPort;
import com.puravida.modules.business.domain.model.BusinessDayStatus;
import com.puravida.modules.business.infrastructure.persistence.BusinessDayStatusEntity;
import com.puravida.modules.business.infrastructure.persistence.BusinessDayStatusJpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class BusinessDayStatusRepositoryAdapter implements BusinessDayStatusRepositoryPort {

    private final BusinessDayStatusJpaRepository jpaRepository;

    public BusinessDayStatusRepositoryAdapter(BusinessDayStatusJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<BusinessDayStatus> findByFecha(LocalDate fecha) {
        return jpaRepository.findByFecha(fecha).map(BusinessDayStatusEntity::toDomain);
    }

    @Override
    public BusinessDayStatus save(BusinessDayStatus status) {
        return jpaRepository.save(BusinessDayStatusEntity.fromDomain(status)).toDomain();
    }
}
