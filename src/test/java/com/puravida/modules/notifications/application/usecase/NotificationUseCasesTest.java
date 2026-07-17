package com.puravida.modules.notifications.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.notifications.application.dto.NotificationResponse;
import com.puravida.modules.notifications.application.dto.ReadAllNotificationsResponse;
import com.puravida.modules.notifications.application.port.out.NotificationRepositoryPort;
import com.puravida.modules.notifications.domain.model.Notification;
import com.puravida.modules.notifications.domain.model.NotificationType;
import com.puravida.modules.users.domain.model.User;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationUseCasesTest {

    @Mock
    private NotificationRepositoryPort notificationRepositoryPort;

    @Mock
    private NotificationAuthorizationService authorizationService;

    private GetMyNotificationsUseCase getUseCase;
    private MarkNotificationReadUseCase markReadUseCase;
    private MarkAllNotificationsReadUseCase markAllReadUseCase;

    @BeforeEach
    void setUp() {
        getUseCase = new GetMyNotificationsUseCase(notificationRepositoryPort, authorizationService);
        markReadUseCase = new MarkNotificationReadUseCase(notificationRepositoryPort, authorizationService);
        markAllReadUseCase = new MarkAllNotificationsReadUseCase(
                notificationRepositoryPort,
                authorizationService
        );
    }

    @Test
    void listsOnlyAuthenticatedUsersNotifications() {
        Notification notification = unreadNotification();
        when(authorizationService.requireActiveUser(authenticatedUser())).thenReturn(user());
        when(notificationRepositoryPort.findByUserId(1)).thenReturn(List.of(notification));

        List<NotificationResponse> response = getUseCase.getMyNotifications(authenticatedUser());

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(10);
        assertThat(response.get(0).leido()).isFalse();
        verify(notificationRepositoryPort).findByUserId(1);
    }

    @Test
    void marksOwnedNotificationAsRead() {
        Notification notification = unreadNotification();
        when(authorizationService.requireActiveUser(authenticatedUser())).thenReturn(user());
        when(notificationRepositoryPort.findByIdAndUserId(10, 1)).thenReturn(Optional.of(notification));
        when(notificationRepositoryPort.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = markReadUseCase.markRead(10, authenticatedUser());

        assertThat(response.leido()).isTrue();
        assertThat(response.leidoEn()).isNotNull();
        verify(notificationRepositoryPort).findByIdAndUserId(10, 1);
    }

    @Test
    void hidesAndDoesNotModifyAnotherUsersNotification() {
        when(authorizationService.requireActiveUser(authenticatedUser())).thenReturn(user());
        when(notificationRepositoryPort.findByIdAndUserId(99, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> markReadUseCase.markRead(99, authenticatedUser()))
                .isInstanceOf(NotFoundException.class);

        verify(notificationRepositoryPort).findByIdAndUserId(99, 1);
        verify(notificationRepositoryPort, never()).save(any());
    }

    @Test
    void marksAllUnreadNotificationsOnlyForAuthenticatedUser() {
        when(authorizationService.requireActiveUser(authenticatedUser())).thenReturn(user());
        when(notificationRepositoryPort.markAllUnreadByUserId(1)).thenReturn(3);

        ReadAllNotificationsResponse response = markAllReadUseCase.markAllRead(authenticatedUser());

        assertThat(response.actualizadas()).isEqualTo(3);
        verify(notificationRepositoryPort).markAllUnreadByUserId(1);
    }

    private AuthenticatedUser authenticatedUser() {
        return new AuthenticatedUser(1, "cliente@example.com", UserRole.CLIENTE);
    }

    private User user() {
        return new User(
                1,
                "Cliente Prueba",
                "cliente@example.com",
                null,
                "hash",
                UserRole.CLIENTE,
                null,
                true,
                true,
                LocalDateTime.of(2026, 7, 17, 12, 0),
                null
        );
    }

    private Notification unreadNotification() {
        return new Notification(
                10,
                1,
                20,
                NotificationType.PEDIDO_ACEPTADO,
                "Pedido aceptado",
                "Tu pedido #20 fue aceptado.",
                false,
                LocalDateTime.of(2026, 7, 17, 12, 5),
                null
        );
    }
}
