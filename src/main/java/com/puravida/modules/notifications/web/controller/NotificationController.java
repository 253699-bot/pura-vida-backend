package com.puravida.modules.notifications.web.controller;

import com.puravida.modules.auth.application.dto.AuthenticatedUser;
import com.puravida.modules.auth.application.port.in.AuthenticateBearerTokenPort;
import com.puravida.modules.notifications.application.dto.NotificationResponse;
import com.puravida.modules.notifications.application.dto.ReadAllNotificationsResponse;
import com.puravida.modules.notifications.application.port.in.GetMyNotificationsPort;
import com.puravida.modules.notifications.application.port.in.MarkAllNotificationsReadPort;
import com.puravida.modules.notifications.application.port.in.MarkNotificationReadPort;
import com.puravida.shared.web.ApiPaths;
import com.puravida.shared.web.response.ApiResponse;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/notifications")
public class NotificationController {

    private final GetMyNotificationsPort getMyNotificationsPort;
    private final MarkNotificationReadPort markNotificationReadPort;
    private final MarkAllNotificationsReadPort markAllNotificationsReadPort;
    private final AuthenticateBearerTokenPort authenticateBearerTokenPort;

    public NotificationController(
            GetMyNotificationsPort getMyNotificationsPort,
            MarkNotificationReadPort markNotificationReadPort,
            MarkAllNotificationsReadPort markAllNotificationsReadPort,
            AuthenticateBearerTokenPort authenticateBearerTokenPort
    ) {
        this.getMyNotificationsPort = getMyNotificationsPort;
        this.markNotificationReadPort = markNotificationReadPort;
        this.markAllNotificationsReadPort = markAllNotificationsReadPort;
        this.authenticateBearerTokenPort = authenticateBearerTokenPort;
    }

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getMyNotifications(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(getMyNotificationsPort.getMyNotifications(authenticatedUser));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(markNotificationReadPort.markRead(id, authenticatedUser));
    }

    @PatchMapping("/read-all")
    public ApiResponse<ReadAllNotificationsResponse> markAllRead(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        AuthenticatedUser authenticatedUser = authenticateBearerTokenPort.authenticate(authorizationHeader);
        return ApiResponse.ok(markAllNotificationsReadPort.markAllRead(authenticatedUser));
    }
}
