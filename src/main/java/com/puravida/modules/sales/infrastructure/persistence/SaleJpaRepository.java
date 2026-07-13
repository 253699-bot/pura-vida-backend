package com.puravida.modules.sales.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SaleJpaRepository
        extends JpaRepository<SaleEntity, Integer>, JpaSpecificationExecutor<SaleEntity> {

    Optional<SaleEntity> findByOrderId(Integer orderId);

    boolean existsByOrderId(Integer orderId);
}
