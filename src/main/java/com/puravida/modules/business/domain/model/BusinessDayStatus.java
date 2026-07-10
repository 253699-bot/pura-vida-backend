package com.puravida.modules.business.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BusinessDayStatus(
        Integer id,
        LocalDate fecha,
        boolean abierto,
        String motivoCierre,
        Integer registradoPor,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {

    public static BusinessDayStatus create(
            LocalDate fecha,
            boolean abierto,
            String motivoCierre,
            Integer registradoPor
    ) {
        return new BusinessDayStatus(
                null,
                fecha,
                abierto,
                motivoCierre,
                registradoPor,
                LocalDateTime.now(),
                null
        );
    }

    public BusinessDayStatus updateStatus(boolean abierto, String motivoCierre, Integer registradoPor) {
        return new BusinessDayStatus(
                id,
                fecha,
                abierto,
                motivoCierre,
                registradoPor,
                creadoEn,
                LocalDateTime.now()
        );
    }
}
