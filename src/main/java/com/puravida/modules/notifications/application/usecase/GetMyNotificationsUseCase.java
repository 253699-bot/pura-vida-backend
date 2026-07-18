package com.puravida.modules.notifications.application.usecase;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.notifications.application.dto.NotificationResponse;
import com.puravida.modules.notifications.application.port.in.GetMyNotificationsPort;
import com.puravida.modules.notifications.application.port.out.NotificationRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMyNotificationsUseCase implements GetMyNotificationsPort {

    private final NotificationRepositoryPort notificationRepositoryPort;
    private final NotificationAuthorizationService authorizationService;

    public GetMyNotificationsUseCase(
            NotificationRepositoryPort notificationRepositoryPort,
            NotificationAuthorizationService authorizationService
    ) {
        this.notificationRepositoryPort = notificationRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(AuthenticatedUser authenticatedUser) {
        User user = authorizationService.requireActiveUser(authenticatedUser);
        return notificationRepositoryPort.findByUserId(user.id()).stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
