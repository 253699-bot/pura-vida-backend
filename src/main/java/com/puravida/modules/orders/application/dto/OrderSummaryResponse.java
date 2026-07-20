package com.puravida.modules.orders.application.dto;

import com.puravida.modules.orders.domain.model.Order;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record OrderSummaryResponse(
        Integer id,
        Integer clienteId,
        String clienteNombre,
        String estado,
        LocalDate fecha,
        LocalTime hora,
        BigDecimal total,
        String notas,
        String motivoRechazo,
        Integer respondidoPor,
        LocalDateTime respondidoEn,
        String tiempoEsperaEstimado,
        Integer canceladoPor,
        LocalDateTime canceladoEn
) {

    public static OrderSummaryResponse from(Order order, String clienteNombre) {
        return new OrderSummaryResponse(
                order.id(),
                order.clienteId(),
                clienteNombre,
                order.estado().databaseValue(),
                order.fecha(),
                order.hora(),
                order.total(),
                order.observaciones(),
                order.motivoRechazo(),
                order.respondidoPor(),
                order.respondidoEn(),
                order.tiempoEsperaEstimado(),
                order.canceladoPor(),
                order.canceladoEn()
        );
    }
}
