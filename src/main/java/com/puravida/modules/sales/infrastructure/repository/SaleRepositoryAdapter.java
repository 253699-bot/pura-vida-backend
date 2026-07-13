package com.puravida.modules.sales.infrastructure.repository;

import com.puravida.modules.sales.application.port.out.SaleRepositoryPort;
import com.puravida.modules.sales.domain.model.Sale;
import com.puravida.modules.sales.domain.model.SaleSearchCriteria;
import com.puravida.modules.sales.infrastructure.persistence.SaleEntity;
import com.puravida.modules.sales.infrastructure.persistence.SaleJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class SaleRepositoryAdapter implements SaleRepositoryPort {

    private final SaleJpaRepository saleJpaRepository;

    public SaleRepositoryAdapter(SaleJpaRepository saleJpaRepository) {
        this.saleJpaRepository = saleJpaRepository;
    }

    @Override
    public Sale save(Sale sale) {
        return saleJpaRepository.save(SaleEntity.fromDomain(sale)).toDomain();
    }

    @Override
    public Optional<Sale> findById(Integer saleId) {
        return saleJpaRepository.findById(saleId).map(SaleEntity::toDomain);
    }

    @Override
    public Optional<Sale> findByOrderId(Integer orderId) {
        return saleJpaRepository.findByOrderId(orderId).map(SaleEntity::toDomain);
    }

    @Override
    public boolean existsByOrderId(Integer orderId) {
        return saleJpaRepository.existsByOrderId(orderId);
    }

    @Override
    public List<Sale> findAll(SaleSearchCriteria criteria) {
        Specification<SaleEntity> specification = (root, query, builder) -> builder.conjunction();
        if (criteria.from() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("fecha"), criteria.from()));
        }
        if (criteria.to() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get("fecha"), criteria.to()));
        }
        if (criteria.source() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("source"), criteria.source()));
        }
        if (criteria.status() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("status"), criteria.status()));
        }

        Sort sort = Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("hora"), Sort.Order.desc("id"));
        return saleJpaRepository.findAll(specification, sort).stream()
                .map(SaleEntity::toDomain)
                .toList();
    }
}
