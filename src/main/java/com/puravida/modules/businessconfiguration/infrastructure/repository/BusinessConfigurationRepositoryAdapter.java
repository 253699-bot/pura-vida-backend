package com.puravida.modules.businessconfiguration.infrastructure.repository;

import com.puravida.modules.businessconfiguration.application.port.out.BusinessConfigurationRepositoryPort;
import com.puravida.modules.businessconfiguration.domain.model.BusinessConfiguration;
import com.puravida.modules.businessconfiguration.infrastructure.persistence.BusinessConfigurationEntity;
import com.puravida.modules.businessconfiguration.infrastructure.persistence.BusinessConfigurationJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class BusinessConfigurationRepositoryAdapter implements BusinessConfigurationRepositoryPort {

    private static final byte SINGLETON_KEY = 1;
    private final BusinessConfigurationJpaRepository jpaRepository;

    public BusinessConfigurationRepositoryAdapter(BusinessConfigurationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<BusinessConfiguration> findSingleton() {
        return jpaRepository.findBySingletonKey(SINGLETON_KEY).map(BusinessConfigurationEntity::toDomain);
    }

    @Override
    public Optional<BusinessConfiguration> findSingletonForUpdate() {
        return jpaRepository.findSingletonForUpdate(SINGLETON_KEY)
                .map(BusinessConfigurationEntity::toDomain);
    }

    @Override
    public BusinessConfiguration save(BusinessConfiguration configuration) {
        return jpaRepository.save(BusinessConfigurationEntity.fromDomain(configuration)).toDomain();
    }
}
