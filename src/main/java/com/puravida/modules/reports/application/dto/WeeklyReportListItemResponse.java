package com.puravida.modules.reports.application.dto;

import com.puravida.modules.reports.domain.model.StoredWeeklyReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record WeeklyReportListItemResponse(
        Integer id,
        LocalDate semanaInicio,
        LocalDate semanaFin,
        Integer totalPedidosApp,
        BigDecimal totalIngresos,
        Integer platilloMasVendidoId,
        LocalDate diaMayorDemanda,
        Integer versionFormato,
        Integer generadoPor,
        LocalDateTime generadoEn,
        boolean snapshotDisponible
) {
    public static WeeklyReportListItemResponse from(StoredWeeklyReport report) {
        return new WeeklyReportListItemResponse(
                report.id(), report.semanaInicio(), report.semanaFin(), report.totalPedidosApp(),
                report.totalIngresos(), report.platilloMasVendidoId(), report.diaMayorDemanda(),
                report.versionFormato(), report.generadoPor(), report.generadoEn(),
                report.resumenJson() != null && !report.resumenJson().isBlank()
        );
    }
}
