package com.puravida.modules.sales.application.port.out;

import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.modules.sales.domain.model.SaleSearchCriteria;
import java.util.List;
import java.util.Optional;

public interface SaleRepositoryPort {

    Sale save(Sale sale);

    Optional<Sale> findById(Integer saleId);

    Optional<Sale> findByOrderId(Integer orderId);

    boolean existsByOrderId(Integer orderId);

    List<Sale> findAll(SaleSearchCriteria criteria);
}
