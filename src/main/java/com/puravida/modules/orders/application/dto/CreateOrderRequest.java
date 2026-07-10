package com.puravida.modules.orders.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "El pedido debe contener al menos un item.")
        List<@Valid CreateOrderItemRequest> items,
        String notas
) {
}
