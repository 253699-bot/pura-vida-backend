package com.puravida.modules.sales.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleJpaRepository
        extends JpaRepository<SaleEntity, Integer>, JpaSpecificationExecutor<SaleEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sale FROM SaleEntity sale WHERE sale.id = :saleId")
    Optional<SaleEntity> findByIdForUpdate(@Param("saleId") Integer saleId);

    Optional<SaleEntity> findByOrderId(Integer orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sale FROM SaleEntity sale WHERE sale.orderId = :orderId")
    Optional<SaleEntity> findByOrderIdForUpdate(@Param("orderId") Integer orderId);

    Optional<SaleEntity> findByRegistradoPorAndIdempotencyKey(Integer actorId, String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT sale FROM SaleEntity sale
            WHERE sale.registradoPor = :actorId AND sale.idempotencyKey = :idempotencyKey
            """)
    Optional<SaleEntity> findByActorAndIdempotencyKeyForUpdate(
            @Param("actorId") Integer actorId,
            @Param("idempotencyKey") String idempotencyKey
    );

    boolean existsByOrderId(Integer orderId);
}
