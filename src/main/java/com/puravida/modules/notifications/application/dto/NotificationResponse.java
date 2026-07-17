package com.puravida.modules.notifications.application.dto;

import com.puravida.modules.notifications.domain.model.Notification;
import java.time.LocalDateTime;

public record NotificationResponse(
        Integer id,
        Integer pedidoId,
        String tipo,
        String titulo,
        String mensaje,
        boolean leido,
        LocalDateTime fecha,
        LocalDateTime leidoEn
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.id(),
                notification.pedidoId(),
                notification.tipo().databaseValue(),
                notification.titulo(),
                notification.mensaje(),
                notification.leida(),
                notification.creadaEn(),
                notification.leidaEn()
        );
    }
}
