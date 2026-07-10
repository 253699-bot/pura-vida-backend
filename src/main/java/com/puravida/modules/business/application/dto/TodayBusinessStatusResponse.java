package com.puravida.modules.business.application.dto;

import com.puravida.modules.business.domain.model.BusinessDayStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TodayBusinessStatusResponse(
        boolean configured,
        Integer id,
        LocalDate fecha,
        Boolean abierto,
        String motivoCierre,
        Integer registradoPor,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {

    public static TodayBusinessStatusResponse from(BusinessDayStatus status) {
        return new TodayBusinessStatusResponse(
                true,
                status.id(),
                status.fecha(),
                status.abierto(),
                status.motivoCierre(),
                status.registradoPor(),
                status.creadoEn(),
                status.actualizadoEn()
        );
    }

    public static TodayBusinessStatusResponse notConfigured(LocalDate fecha) {
        return new TodayBusinessStatusResponse(false, null, fecha, null, null, null, null, null);
    }
}
