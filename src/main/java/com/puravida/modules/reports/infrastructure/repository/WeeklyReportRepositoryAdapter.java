package com.puravida.modules.reports.infrastructure.repository;

import com.puravida.modules.reports.application.port.out.WeeklyReportRepositoryPort;
import com.puravida.modules.reports.domain.model.StoredWeeklyReport;
import com.puravida.modules.reports.infrastructure.persistence.WeeklyReportEntity;
import com.puravida.modules.reports.infrastructure.persistence.WeeklyReportJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class WeeklyReportRepositoryAdapter implements WeeklyReportRepositoryPort {

    private final WeeklyReportJpaRepository jpaRepository;

    public WeeklyReportRepositoryAdapter(WeeklyReportJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<StoredWeeklyReport> findById(Integer reportId) {
        return jpaRepository.findById(reportId).map(WeeklyReportEntity::toDomain);
    }

    @Override
    public List<StoredWeeklyReport> findAllOrderByGeneratedAtDesc() {
        return jpaRepository.findAllByOrderByGeneradoEnDescIdDesc().stream()
                .map(WeeklyReportEntity::toDomain)
                .toList();
    }

    @Override
    public StoredWeeklyReport create(StoredWeeklyReport candidate) {
        return jpaRepository.save(WeeklyReportEntity.fromDomain(candidate)).toDomain();
    }
}