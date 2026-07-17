package com.puravida.modules.notifications.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.notifications.application.dto.NotificationResponse;
import java.util.List;

public interface GetMyNotificationsPort {

    List<NotificationResponse> getMyNotifications(AuthenticatedUser authenticatedUser);
}
