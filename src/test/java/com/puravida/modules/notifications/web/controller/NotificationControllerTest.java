package com.puravida.modules.notifications.web.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.notifications.application.dto.NotificationResponse;
import com.puravida.modules.notifications.application.dto.ReadAllNotificationsResponse;
import com.puravida.modules.notifications.application.port.in.GetMyNotificationsPort;
import com.puravida.modules.notifications.application.port.in.MarkAllNotificationsReadPort;
import com.puravida.modules.notifications.application.port.in.MarkNotificationReadPort;
import com.puravida.modules.users.domain.model.UserRole;
import com.puravida.shared.domain.exception.NotFoundException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import com.puravida.shared.web.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = NotificationController.class, properties = "debug=false")
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetMyNotificationsPort getMyNotificationsPort;

    @MockitoBean
    private MarkNotificationReadPort markNotificationReadPort;

    @MockitoBean
    private MarkAllNotificationsReadPort markAllNotificationsReadPort;

    @MockitoBean
    private AuthenticateBearerTokenPort authenticateBearerTokenPort;

    @Test
    void listsAuthenticatedUsersNotifications() throws Exception {
        AuthenticatedUser user = authenticatedUser();
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(user);
        when(getMyNotificationsPort.getMyNotifications(user)).thenReturn(List.of(unreadResponse()));

        mockMvc.perform(get("/api/v1/notifications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data[0].id", is(10)))
                .andExpect(jsonPath("$.data[0].pedidoId", is(20)))
                .andExpect(jsonPath("$.data[0].tipo", is("pedido_aceptado")))
                .andExpect(jsonPath("$.data[0].leido", is(false)));
    }

    @Test
    void marksOwnedNotificationAsRead() throws Exception {
        AuthenticatedUser user = authenticatedUser();
        NotificationResponse read = new NotificationResponse(
                10,
                20,
                "pedido_aceptado",
                "Pedido aceptado",
                "Tu pedido #20 fue aceptado.",
                true,
                LocalDateTime.of(2026, 7, 17, 12, 5),
                LocalDateTime.of(2026, 7, 17, 12, 10)
        );
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(user);
        when(markNotificationReadPort.markRead(10, user)).thenReturn(read);

        mockMvc.perform(patch("/api/v1/notifications/10/read")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(10)))
                .andExpect(jsonPath("$.data.leido", is(true)))
                .andExpect(jsonPath("$.data.leidoEn", is("2026-07-17T12:10:00")));
    }

    @Test
    void hidesAnotherUsersNotificationAsNotFound() throws Exception {
        AuthenticatedUser user = authenticatedUser();
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(user);
        when(markNotificationReadPort.markRead(99, user))
                .thenThrow(new NotFoundException("No se encontro la notificacion."));

        mockMvc.perform(patch("/api/v1/notifications/99/read")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    @Test
    void marksAllAuthenticatedUsersNotificationsAsRead() throws Exception {
        AuthenticatedUser user = authenticatedUser();
        when(authenticateBearerTokenPort.authenticate("Bearer client-token")).thenReturn(user);
        when(markAllNotificationsReadPort.markAllRead(user)).thenReturn(new ReadAllNotificationsResponse(2));

        mockMvc.perform(patch("/api/v1/notifications/read-all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer client-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actualizadas", is(2)));
    }

    @Test
    void returnsUnauthorizedWithoutToken() throws Exception {
        when(authenticateBearerTokenPort.authenticate(null))
                .thenThrow(new UnauthorizedException("Token de autenticacion requerido."));

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("ERROR")));
    }

    private AuthenticatedUser authenticatedUser() {
        return new AuthenticatedUser(1, "cliente@example.com", UserRole.CLIENTE);
    }

    private NotificationResponse unreadResponse() {
        return new NotificationResponse(
                10,
                20,
                "pedido_aceptado",
                "Pedido aceptado",
                "Tu pedido #20 fue aceptado.",
                false,
                LocalDateTime.of(2026, 7, 17, 12, 5),
                null
        );
    }
}
