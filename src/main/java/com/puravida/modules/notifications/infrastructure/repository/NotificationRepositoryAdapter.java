package com.puravida.modules.notifications.infrastructure.repository;

import com.puravida.modules.notifications.application.port.out.NotificationRepositoryPort;
import com.puravida.modules.notifications.domain.model.Notification;
import com.puravida.modules.notifications.infrastructure.persistence.NotificationEntity;
import com.puravida.modules.notifications.infrastructure.persistence.NotificationJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository notificationJpaRepository;

    public NotificationRepositoryAdapter(NotificationJpaRepository notificationJpaRepository) {
        this.notificationJpaRepository = notificationJpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        return notificationJpaRepository.save(NotificationEntity.fromDomain(notification)).toDomain();
    }

    @Override
    public List<Notification> saveAll(List<Notification> notifications) {
        return notificationJpaRepository.saveAll(notifications.stream()
                        .map(NotificationEntity::fromDomain)
                        .toList())
                .stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findByUserId(Integer userId) {
        return notificationJpaRepository.findByUsuarioIdOrderByCreadoEnDescIdDesc(userId).stream()
                .map(NotificationEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Notification> findByIdAndUserId(Integer notificationId, Integer userId) {
        return notificationJpaRepository.findByIdAndUsuarioId(notificationId, userId)
                .map(NotificationEntity::toDomain);
    }

    @Override
    public int markAllUnreadByUserId(Integer userId) {
        return notificationJpaRepository.markAllUnreadByUsuarioId(userId);
    }
}
