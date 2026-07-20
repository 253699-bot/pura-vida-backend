package com.puravida.modules.reports.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyReportJpaRepository extends JpaRepository<WeeklyReportEntity, Integer> {

    List<WeeklyReportEntity> findAllByOrderByGeneradoEnDescIdDesc();
}