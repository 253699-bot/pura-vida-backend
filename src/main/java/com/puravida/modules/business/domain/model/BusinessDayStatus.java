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
        LocalDateTime actualizadoEn,
        LocalDateTime cicloIniciadoEn
) {
    public BusinessDayStatus(
            Integer id,
            LocalDate fecha,
            boolean abierto,
            String motivoCierre,
            Integer registradoPor,
            LocalDateTime creadoEn,
            LocalDateTime actualizadoEn
    ) {
        this(id, fecha, abierto, motivoCierre, registradoPor, creadoEn, actualizadoEn, null);
    }

    public static BusinessDayStatus create(
            LocalDate fecha,
            boolean abierto,
            String motivoCierre,
            Integer registradoPor
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new BusinessDayStatus(
                null,
                fecha,
                abierto,
                motivoCierre,
                registradoPor,
                now,
                null,
                abierto ? now : null
        );
    }

    public BusinessDayStatus updateStatus(boolean abierto, String motivoCierre, Integer registradoPor) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextCycleStartedAt = cicloIniciadoEn;
        if (abierto && (!this.abierto || cicloIniciadoEn == null)) {
            nextCycleStartedAt = now;
        }

        return new BusinessDayStatus(
                id,
                fecha,
                abierto,
                motivoCierre,
                registradoPor,
                creadoEn,
                now,
                nextCycleStartedAt
        );
    }
}