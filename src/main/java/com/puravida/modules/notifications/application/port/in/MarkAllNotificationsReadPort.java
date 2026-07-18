package com.puravida.modules.notifications.application.port.in;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.notifications.application.dto.ReadAllNotificationsResponse;

public interface MarkAllNotificationsReadPort {

    ReadAllNotificationsResponse markAllRead(AuthenticatedUser authenticatedUser);
}
