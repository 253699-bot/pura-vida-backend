package com.puravida.modules.notifications.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.notifications.application.dto.ReadAllNotificationsResponse;
import com.puravida.modules.notifications.application.port.in.MarkAllNotificationsReadPort;
import com.puravida.modules.notifications.application.port.out.NotificationRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarkAllNotificationsReadUseCase implements MarkAllNotificationsReadPort {

    private final NotificationRepositoryPort notificationRepositoryPort;
    private final NotificationAuthorizationService authorizationService;

    public MarkAllNotificationsReadUseCase(
            NotificationRepositoryPort notificationRepositoryPort,
            NotificationAuthorizationService authorizationService
    ) {
        this.notificationRepositoryPort = notificationRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public ReadAllNotificationsResponse markAllRead(AuthenticatedUser authenticatedUser) {
        User user = authorizationService.requireActiveUser(authenticatedUser);
        return new ReadAllNotificationsResponse(notificationRepositoryPort.markAllUnreadByUserId(user.id()));
    }
}
