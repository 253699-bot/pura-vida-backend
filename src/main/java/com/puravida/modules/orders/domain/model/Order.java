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
        String categoriaRechazo,
        Integer respondidoPor,
        LocalDateTime respondidoEn,
        Integer canceladoPor,
        LocalDateTime canceladoEn,
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
                null,
                null,
                null,
                observaciones,
                LocalDateTime.now()
        );
    }

    public Order accept(Integer encargadoId, String tiempoEsperaEstimado) {
        return new Order(
                id,
                clienteId,
                fecha,
                hora,
                OrderStatus.ACEPTADO,
                total,
                tiempoEsperaEstimado,
                null,
                null,
                encargadoId,
                LocalDateTime.now(),
                null,
                null,
                observaciones,
                creadoEn
        );
    }

    public Order reject(Integer encargadoId, String categoriaRechazo, String motivoRechazo) {
        return new Order(
                id,
                clienteId,
                fecha,
                hora,
                OrderStatus.RECHAZADO,
                total,
                tiempoEsperaEstimado,
                motivoRechazo,
                categoriaRechazo,
                encargadoId,
                LocalDateTime.now(),
                null,
                null,
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
                categoriaRechazo,
                respondidoPor,
                respondidoEn,
                canceladoPor,
                canceladoEn,
                observaciones,
                creadoEn
        );
    }

    public Order cancel(Integer encargadoId) {
        return new Order(
                id,
                clienteId,
                fecha,
                hora,
                OrderStatus.CANCELADO,
                total,
                tiempoEsperaEstimado,
                motivoRechazo,
                categoriaRechazo,
                respondidoPor,
                respondidoEn,
                encargadoId,
                LocalDateTime.now(),
                observaciones,
                creadoEn
        );
    }
}
