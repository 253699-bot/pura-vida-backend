package com.puravida.modules.auth.application.dto;

import com.puravida.modules.users.domain.model.UserRole;

public record AuthenticatedUser(
        Integer userId,
        String correo,
        UserRole rol
) {
}
