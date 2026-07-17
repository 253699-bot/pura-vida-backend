package com.puravida.modules.notifications.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.notifications.application.dto.NotificationResponse;
import com.puravida.modules.notifications.application.port.in.MarkNotificationReadPort;
import com.puravida.modules.notifications.application.port.out.NotificationRepositoryPort;
import com.puravida.modules.notifications.domain.model.Notification;
import com.puravida.modules.users.domain.model.User;
import com.puravida.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarkNotificationReadUseCase implements MarkNotificationReadPort {

    private final NotificationRepositoryPort notificationRepositoryPort;
    private final NotificationAuthorizationService authorizationService;

    public MarkNotificationReadUseCase(
            NotificationRepositoryPort notificationRepositoryPort,
            NotificationAuthorizationService authorizationService
    ) {
        this.notificationRepositoryPort = notificationRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public NotificationResponse markRead(Integer notificationId, AuthenticatedUser authenticatedUser) {
        User user = authorizationService.requireActiveUser(authenticatedUser);
        Notification notification = notificationRepositoryPort.findByIdAndUserId(notificationId, user.id())
                .orElseThrow(() -> new NotFoundException("No se encontro la notificacion."));

        if (notification.leida()) {
            return NotificationResponse.from(notification);
        }
        return NotificationResponse.from(notificationRepositoryPort.save(notification.markRead()));
    }
}
