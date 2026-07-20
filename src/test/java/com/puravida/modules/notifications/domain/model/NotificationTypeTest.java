package com.puravida.modules.notifications.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NotificationTypeTest {

    @Test
    void fallsBackToSystemForLegacyBlankOrUnknownValues() {
        assertThat(NotificationType.fromDatabaseValue(null)).isEqualTo(NotificationType.SISTEMA);
        assertThat(NotificationType.fromDatabaseValue(" ")).isEqualTo(NotificationType.SISTEMA);
        assertThat(NotificationType.fromDatabaseValue("tipo_legacy")).isEqualTo(NotificationType.SISTEMA);
    }

    @Test
    void resolvesSupportedDatabaseValue() {
        assertThat(NotificationType.fromDatabaseValue("pedido_aceptado"))
                .isEqualTo(NotificationType.PEDIDO_ACEPTADO);
    }
}
