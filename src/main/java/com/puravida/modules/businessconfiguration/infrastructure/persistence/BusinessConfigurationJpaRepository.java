package com.puravida.modules.businessconfiguration.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusinessConfigurationJpaRepository
        extends JpaRepository<BusinessConfigurationEntity, Integer> {

    Optional<BusinessConfigurationEntity> findBySingletonKey(Byte singletonKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT entity FROM BusinessConfigurationEntity entity WHERE entity.singletonKey = :singletonKey")
    Optional<BusinessConfigurationEntity> findSingletonForUpdate(
            @Param("singletonKey") Byte singletonKey
    );
}
