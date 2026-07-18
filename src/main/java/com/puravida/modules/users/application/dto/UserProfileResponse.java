package com.puravida.modules.users.application.dto;

import com.puravida.modules.users.domain.model.User;

public record UserProfileResponse(
        Integer id,
        String nombre,
        String correo,
        String telefono,
        String rol,
        String iconoPerfil,
        boolean notificacionesActivas,
        boolean activo
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
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
