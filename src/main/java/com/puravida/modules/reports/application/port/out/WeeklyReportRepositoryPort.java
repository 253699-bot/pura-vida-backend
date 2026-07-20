package com.puravida.modules.reports.application.port.out;

import com.puravida.modules.reports.domain.model.StoredWeeklyReport;
import java.util.List;
import java.util.Optional;

public interface WeeklyReportRepositoryPort {
    Optional<StoredWeeklyReport> findById(Integer reportId);
    List<StoredWeeklyReport> findAllOrderByGeneratedAtDesc();
    StoredWeeklyReport create(StoredWeeklyReport candidate);
}