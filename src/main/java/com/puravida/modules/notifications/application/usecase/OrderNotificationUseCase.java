package com.puravida.modules.notifications.application.usecase;

import com.puravida.modules.notifications.application.port.in.OrderNotificationPort;
import com.puravida.modules.notifications.application.port.out.NotificationRepositoryPort;
import com.puravida.modules.notifications.domain.model.Notification;
import com.puravida.modules.notifications.domain.model.NotificationType;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderNotificationUseCase implements OrderNotificationPort {

    private final NotificationRepositoryPort notificationRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public OrderNotificationUseCase(
            NotificationRepositoryPort notificationRepositoryPort,
            UserRepositoryPort userRepositoryPort
    ) {
        this.notificationRepositoryPort = notificationRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public void notifyOrderCreated(Integer orderId) {
        List<Notification> notifications = userRepositoryPort
                .findActiveWithNotificationsByRole(UserRole.ENCARGADA)
                .stream()
                .map(user -> Notification.create(
                        user.id(),
                        orderId,
                        NotificationType.PEDIDO_CREADO,
                        "Nuevo pedido #" + orderId,
                        "Se creo el pedido #" + orderId + " y esta pendiente de revision."
                ))
                .toList();
        if (!notifications.isEmpty()) {
            notificationRepositoryPort.saveAll(notifications);
        }
    }

    @Override
    public void notifyOrderAccepted(Integer orderId, Integer clientId) {
        notifyClient(
                clientId,
                orderId,
                NotificationType.PEDIDO_ACEPTADO,
                "Pedido aceptado",
                "Tu pedido #" + orderId + " fue aceptado."
        );
    }

    @Override
    public void notifyOrderRejected(Integer orderId, Integer clientId, String reason) {
        String message = "Tu pedido #" + orderId + " fue rechazado.";
        if (reason != null && !reason.isBlank()) {
            message += " Motivo: " + reason.trim();
        }
        notifyClient(
                clientId,
                orderId,
                NotificationType.PEDIDO_RECHAZADO,
                "Pedido rechazado",
                message
        );
    }

    private void notifyClient(
            Integer clientId,
            Integer orderId,
            NotificationType type,
            String title,
            String message
    ) {
        if (clientId == null) {
            return;
        }
        userRepositoryPort.findById(clientId)
                .filter(User::activo)
                .filter(User::notificacionesActivas)
                .ifPresent(user -> notificationRepositoryPort.save(Notification.create(
                        user.id(),
                        orderId,
                        type,
                        title,
                        message
                )));
    }
}
