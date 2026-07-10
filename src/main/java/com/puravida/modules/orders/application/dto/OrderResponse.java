package com.puravida.modules.orders.application.dto;

import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record OrderResponse(
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
        List<OrderItemResponse> items
) {

    public static OrderResponse from(Order order, String clienteNombre, List<OrderItem> items) {
        return new OrderResponse(
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
                items.stream().map(OrderItemResponse::from).toList()
        );
    }
}
