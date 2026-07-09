package com.puravida.modules.auth.application.dto;

import com.puravida.modules.users.domain.model.User;

public record UserSummaryResponse(
        Integer id,
        String nombre,
        String correo,
        String telefono,
        String rol,
        String iconoPerfil,
        boolean notificacionesActivas,
        boolean activo
) {

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.id(),
                user.nombre(),
                user.correo(),
                user.telefono(),
                user.rol().databaseValue(),
                user.iconoPerfil(),
                user.notificacionesActivas(),
                user.activo()
        );
    }
}
