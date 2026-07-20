package com.puravida.modules.sales.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManualSaleLineJpaRepository extends JpaRepository<ManualSaleLineEntity, Integer> {

    List<ManualSaleLineEntity> findBySaleIdOrderByIdAsc(Integer saleId);
}
