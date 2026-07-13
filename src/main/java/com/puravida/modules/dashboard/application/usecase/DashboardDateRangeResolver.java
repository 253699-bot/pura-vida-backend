package com.puravida.modules.dashboard.application.usecase;

import com.puravida.modules.dashboard.domain.exception.DashboardValidationException;
import com.puravida.modules.dashboard.domain.model.DashboardDateRange;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class DashboardDateRangeResolver {

    private static final int DEFAULT_DAYS = 7;
    private static final int MAX_DAYS = 366;

    public DashboardDateRange resolve(String from, String to) {
        if (isBlank(from) && isBlank(to)) {
            LocalDate today = LocalDate.now();
            return new DashboardDateRange(today.minusDays(DEFAULT_DAYS - 1L), today);
        }
        if (isBlank(from) || isBlank(to)) {
            throw new DashboardValidationException("Los parametros from y to deben enviarse juntos.");
        }

        LocalDate fromDate = parse(from, "from");
        LocalDate toDate = parse(to, "to");
        if (fromDate.isAfter(toDate)) {
            throw new DashboardValidationException("La fecha from no puede ser posterior a to.");
        }
        if (ChronoUnit.DAYS.between(fromDate, toDate) + 1 > MAX_DAYS) {
            throw new DashboardValidationException("El rango maximo permitido es de 366 dias.");
        }
        return new DashboardDateRange(fromDate, toDate);
    }

    private LocalDate parse(String value, String field) {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new DashboardValidationException("La fecha " + field + " debe usar formato YYYY-MM-DD.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
