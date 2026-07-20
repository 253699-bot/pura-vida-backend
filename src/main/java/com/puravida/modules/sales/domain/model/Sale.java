package com.puravida.modules.sales.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record Sale(
        Integer id,
        Integer orderId,
        SaleSource source,
        SaleStatus status,
        String idempotencyKey,
        LocalDate fecha,
        LocalTime hora,
        BigDecimal total,
        Integer registradoPor,
        String observaciones,
        String motivoAnulacion,
        LocalDateTime anuladaEn,
        Integer usuarioAnuloId,
        LocalDateTime creadoEn
) {

    public static Sale createRemote(Integer orderId, BigDecimal total, Integer registradoPor) {
        LocalDateTime now = LocalDateTime.now();
        return new Sale(
                null,
                orderId,
                SaleSource.REMOTA,
                SaleStatus.ACTIVA,
                null,
                now.toLocalDate(),
                now.toLocalTime(),
                total,
                registradoPor,
                null,
                null,
                null,
                null,
                now
        );
    }

    public static Sale createManual(
            BigDecimal total,
            Integer registradoPor,
            String observaciones,
            String idempotencyKey
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Sale(
                null,
                null,
                SaleSource.MANUAL_FONDA,
                SaleStatus.ACTIVA,
                idempotencyKey,
                now.toLocalDate(),
                now.toLocalTime(),
                total,
                registradoPor,
                observaciones,
                null,
                null,
                null,
                now
        );
    }

    public Sale cancel(String motivo, Integer usuarioAnuloId) {
        return new Sale(
                id,
                orderId,
                source,
                SaleStatus.ANULADA,
                idempotencyKey,
                fecha,
                hora,
                total,
                registradoPor,
                observaciones,
                motivo,
                LocalDateTime.now(),
                usuarioAnuloId,
                creadoEn
        );
    }
}
