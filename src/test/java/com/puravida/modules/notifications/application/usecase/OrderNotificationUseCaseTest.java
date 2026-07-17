package com.puravida.modules.notifications.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.notifications.application.port.out.NotificationRepositoryPort;
import com.puravida.modules.notifications.domain.model.Notification;
import com.puravida.modules.notifications.domain.model.NotificationType;
import com.puravida.modules.users.application.port.out.UserRepositoryPort;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderNotificationUseCaseTest {

    @Mock
    private NotificationRepositoryPort notificationRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private OrderNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new OrderNotificationUseCase(notificationRepositoryPort, userRepositoryPort);
    }

    @Test
    void createsOneNotificationForEachEnabledActiveManager() {
        when(userRepositoryPort.findActiveWithNotificationsByRole(UserRole.ENCARGADA))
                .thenReturn(List.of(user(2, UserRole.ENCARGADA, true, true)));

        useCase.notifyOrderCreated(30);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepositoryPort).saveAll(captor.capture());
        assertThat(captor.getValue()).singleElement().satisfies(notification -> {
            assertThat(notification.usuarioId()).isEqualTo(2);
            assertThat(notification.pedidoId()).isEqualTo(30);
            assertThat(notification.tipo()).isEqualTo(NotificationType.PEDIDO_CREADO);
        });
    }

    @Test
    void notifiesEnabledClientWhenOrderIsRejected() {
        when(userRepositoryPort.findById(1))
                .thenReturn(Optional.of(user(1, UserRole.CLIENTE, true, true)));
        when(notificationRepositoryPort.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.notifyOrderRejected(30, 1, "Sin existencias");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().tipo()).isEqualTo(NotificationType.PEDIDO_RECHAZADO);
        assertThat(captor.getValue().mensaje()).contains("Sin existencias");
    }

    @Test
    void doesNotCreateNewNotificationsForDisabledClient() {
        when(userRepositoryPort.findById(1))
                .thenReturn(Optional.of(user(1, UserRole.CLIENTE, false, true)));

        useCase.notifyOrderAccepted(30, 1);

        verify(notificationRepositoryPort, never()).save(any());
    }

    private User user(Integer id, UserRole role, boolean notificationsEnabled, boolean active) {
        return new User(
                id,
                "Usuario Prueba",
                "usuario" + id + "@example.com",
                null,
                "hash",
                role,
                null,
                notificationsEnabled,
                active,
                LocalDateTime.of(2026, 7, 17, 12, 0),
                null
        );
    }
}
