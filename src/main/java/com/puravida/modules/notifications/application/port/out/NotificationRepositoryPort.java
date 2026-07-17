package com.puravida.modules.notifications.application.port.out;

import com.puravida.modules.notifications.domain.model.Notification;
import java.util.List;
import java.util.Optional;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    List<Notification> saveAll(List<Notification> notifications);

    List<Notification> findByUserId(Integer userId);

    Optional<Notification> findByIdAndUserId(Integer notificationId, Integer userId);

    int markAllUnreadByUserId(Integer userId);
}
