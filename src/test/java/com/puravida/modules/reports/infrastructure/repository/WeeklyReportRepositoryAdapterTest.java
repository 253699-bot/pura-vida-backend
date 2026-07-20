package com.puravida.modules.reports.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.reports.domain.model.StoredWeeklyReport;
import com.puravida.modules.reports.infrastructure.persistence.WeeklyReportEntity;
import com.puravida.modules.reports.infrastructure.persistence.WeeklyReportJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeeklyReportRepositoryAdapterTest {

    @Mock WeeklyReportJpaRepository jpaRepository;
    @InjectMocks WeeklyReportRepositoryAdapter adapter;

    @Test
    void persistsANewSnapshotWithoutLookingUpExistingWeek() {
        StoredWeeklyReport candidate = candidate();
        when(jpaRepository.save(any(WeeklyReportEntity.class))).thenAnswer(invocation -> {
            WeeklyReportEntity entity = invocation.getArgument(0);
            return WeeklyReportEntity.fromDomain(withId(entity.toDomain(), 15));
        });

        StoredWeeklyReport persisted = adapter.create(candidate);

        assertThat(persisted.id()).isEqualTo(15);
        assertThat(persisted.resumenJson()).isEqualTo("snapshot-json");
        ArgumentCaptor<WeeklyReportEntity> captor = ArgumentCaptor.forClass(WeeklyReportEntity.class);
        verify(jpaRepository).save(captor.capture());
        StoredWeeklyReport savedCandidate = captor.getValue().toDomain();
        assertThat(savedCandidate.id()).isNull();
        assertThat(savedCandidate.semanaInicio()).isEqualTo(candidate.semanaInicio());
        assertThat(savedCandidate.semanaFin()).isEqualTo(candidate.semanaFin());
        assertThat(savedCandidate.totalIngresos()).isEqualByComparingTo(candidate.totalIngresos());
    }

    private StoredWeeklyReport candidate() {
        return new StoredWeeklyReport(
                null, LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 12),
                2, new BigDecimal("300.00"), null, null, null, "snapshot-json", 1, 4,
                LocalDateTime.of(2026, 7, 12, 18, 0)
        );
    }

    private StoredWeeklyReport withId(StoredWeeklyReport report, Integer id) {
        return new StoredWeeklyReport(
                id, report.semanaInicio(), report.semanaFin(), report.totalPedidosApp(),
                report.totalIngresos(), report.platilloMasVendidoId(), report.diaMayorDemanda(),
                report.rutaArchivo(), report.resumenJson(), report.versionFormato(), report.generadoPor(),
                report.generadoEn()
        );
    }
}