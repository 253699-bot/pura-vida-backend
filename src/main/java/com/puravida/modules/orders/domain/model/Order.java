package com.puravida.modules.orders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record Order(
        Integer id,
        Integer clienteId,
        LocalDate fecha,
        LocalTime hora,
        OrderStatus estado,
        BigDecimal total,
        String tiempoEsperaEstimado,
        String motivoRechazo,
        Integer respondidoPor,
        LocalDateTime respondidoEn,
        String observaciones,
        LocalDateTime creadoEn
) {

    public static Order create(
            Integer clienteId,
            LocalDate fecha,
            LocalTime hora,
            BigDecimal total,
            String observaciones
    ) {
        return new Order(
                null,
                clienteId,
                fecha,
                hora,
                OrderStatus.PENDIENTE,
                total,
                null,
                null,
                null,
                null,
                observaciones,
                LocalDateTime.now()
        );
    }

    public Order accept(Integer encargadoId) {
        return new Order(
                id,
                clienteId,
                fecha,
                hora,
                OrderStatus.ACEPTADO,
                total,
                tiempoEsperaEstimado,
                null,
                encargadoId,
                LocalDateTime.now(),
                observaciones,
                creadoEn
        );
    }

    public Order reject(Integer encargadoId, String motivoRechazo) {
        return new Order(
                id,
                clienteId,
                fecha,
                hora,
                OrderStatus.RECHAZADO,
                total,
                tiempoEsperaEstimado,
                motivoRechazo,
                encargadoId,
                LocalDateTime.now(),
                observaciones,
                creadoEn
        );
    }

    public Order complete() {
        return new Order(
                id,
                clienteId,
                fecha,
                hora,
                OrderStatus.FINALIZADO,
                total,
                tiempoEsperaEstimado,
                motivoRechazo,
                respondidoPor,
                respondidoEn,
                observaciones,
                creadoEn
        );
    }
}
