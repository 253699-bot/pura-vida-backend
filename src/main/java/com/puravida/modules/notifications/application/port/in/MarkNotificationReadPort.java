package com.puravida.modules.notifications.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.notifications.application.dto.NotificationResponse;

public interface MarkNotificationReadPort {

    NotificationResponse markRead(Integer notificationId, AuthenticatedUser authenticatedUser);
}
