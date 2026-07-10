package com.puravida.modules.orders.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.orders.domain.model.Order;
import com.puravida.modules.orders.domain.model.OrderItem;
import com.puravida.modules.orders.domain.model.OrderStatus;
import com.puravida.modules.orders.domain.model.OrderableMenuItem;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

final class TestOrderData {

    private TestOrderData() {
    }

    static AuthenticatedUser authenticatedClient() {
        return new AuthenticatedUser(1, "cliente@example.com", UserRole.CLIENTE);
    }

    static AuthenticatedUser authenticatedEncargada() {
        return new AuthenticatedUser(2, "encargada@example.com", UserRole.ENCARGADA);
    }

    static User client() {
        return user(1, "Cliente Prueba", UserRole.CLIENTE);
    }

    static User encargada() {
        return user(2, "Encargada Prueba", UserRole.ENCARGADA);
    }

    static User user(Integer id, String nombre, UserRole role) {
        return new User(
                id,
                nombre,
                nombre.toLowerCase().replace(' ', '.') + "@example.com",
                null,
                "hash",
                role,
                null,
                true,
                true,
                LocalDateTime.of(2026, 7, 10, 8, 0),
                null
        );
    }

    static Order pendingOrder() {
        return new Order(
                10,
                1,
                LocalDate.of(2026, 7, 10),
                LocalTime.of(12, 0),
                OrderStatus.PENDIENTE,
                new BigDecimal("130.00"),
                null,
                null,
                null,
                null,
                "Sin cebolla",
                LocalDateTime.of(2026, 7, 10, 12, 0)
        );
    }

    static Order acceptedOrder() {
        Order order = pendingOrder();
        return new Order(
                order.id(),
                order.clienteId(),
                order.fecha(),
                order.hora(),
                OrderStatus.ACEPTADO,
                order.total(),
                null,
                null,
                2,
                LocalDateTime.of(2026, 7, 10, 12, 5),
                order.observaciones(),
                order.creadoEn()
        );
    }

    static OrderItem orderItem() {
        return new OrderItem(
                50,
                10,
                5,
                20,
                "Tacos",
                2,
                new BigDecimal("65.00"),
                new BigDecimal("130.00")
        );
    }

    static OrderableMenuItem menuItem() {
        return new OrderableMenuItem(
                20,
                LocalDate.now(),
                5,
                "Tacos",
                new BigDecimal("65.00"),
                true,
                true
        );
    }
}
