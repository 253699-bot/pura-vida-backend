package com.puravida.modules.orders.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void acceptanceAndCancellationPreserveResponseAuditAndAddCancellationAudit() {
        Order accepted = Order.create(
                1,
                LocalDate.of(2026, 7, 18),
                LocalTime.of(12, 0),
                new BigDecimal("130.00"),
                null
        ).accept(2, "25 minutos");

        Order cancelled = accepted.cancel(3);

        assertThat(cancelled.estado()).isEqualTo(OrderStatus.CANCELADO);
        assertThat(cancelled.tiempoEsperaEstimado()).isEqualTo("25 minutos");
        assertThat(cancelled.respondidoPor()).isEqualTo(2);
        assertThat(cancelled.respondidoEn()).isEqualTo(accepted.respondidoEn());
        assertThat(cancelled.canceladoPor()).isEqualTo(3);
        assertThat(cancelled.canceladoEn()).isNotNull();
    }
}
