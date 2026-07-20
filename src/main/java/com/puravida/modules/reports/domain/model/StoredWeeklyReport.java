package com.puravida.modules.reports.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record StoredWeeklyReport(
        Integer id,
        LocalDate semanaInicio,
        LocalDate semanaFin,
        Integer totalPedidosApp,
        BigDecimal totalIngresos,
        Integer platilloMasVendidoId,
        LocalDate diaMayorDemanda,
        String rutaArchivo,
        String resumenJson,
        Integer versionFormato,
        Integer generadoPor,
        LocalDateTime generadoEn
) {
}
