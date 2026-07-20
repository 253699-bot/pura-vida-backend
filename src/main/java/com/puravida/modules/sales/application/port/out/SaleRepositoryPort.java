package com.puravida.modules.sales.application.port.out;

import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.modules.sales.domain.model.SaleSearchCriteria;
import com.puravida.modules.sales.domain.model.ManualSaleLine;
import java.util.List;
import java.util.Optional;

public interface SaleRepositoryPort {

    Sale save(Sale sale);

    Optional<Sale> findById(Integer saleId);

    Optional<Sale> findByIdForUpdate(Integer saleId);

    Optional<Sale> findByOrderId(Integer orderId);

    Optional<Sale> findByOrderIdForUpdate(Integer orderId);

    Optional<Sale> findByActorAndIdempotencyKey(Integer actorId, String idempotencyKey);

    Optional<Sale> findByActorAndIdempotencyKeyForUpdate(Integer actorId, String idempotencyKey);

    boolean existsByOrderId(Integer orderId);

    List<ManualSaleLine> saveLines(List<ManualSaleLine> lines);

    List<ManualSaleLine> findLinesBySaleId(Integer saleId);

    List<Sale> findAll(SaleSearchCriteria criteria);
}
