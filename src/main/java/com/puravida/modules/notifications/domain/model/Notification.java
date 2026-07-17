package com.puravida.modules.notifications.domain.model;

import java.time.LocalDateTime;

public record Notification(
        Integer id,
        Integer usuarioId,
        Integer pedidoId,
        NotificationType tipo,
        String titulo,
        String mensaje,
        boolean leida,
        LocalDateTime creadaEn,
        LocalDateTime leidaEn
) {

    public static Notification create(
            Integer usuarioId,
            Integer pedidoId,
            NotificationType tipo,
            String titulo,
            String mensaje
    ) {
        return new Notification(
                null,
                usuarioId,
                pedidoId,
                tipo,
                titulo,
                mensaje,
                false,
                LocalDateTime.now(),
                null
        );
    }

    public Notification markRead() {
        if (leida) {
            return this;
        }
        return new Notification(
                id,
                usuarioId,
                pedidoId,
                tipo,
                titulo,
                mensaje,
                true,
                creadaEn,
                LocalDateTime.now()
        );
    }
}
