package com.puravida.modules.users.domain.model;

import java.time.LocalDateTime;

public record User(
        Integer id,
        String nombre,
        String correo,
        String telefono,
        String passwordHash,
        UserRole rol,
        String iconoPerfil,
        boolean notificacionesActivas,
        boolean activo,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn
) {

    public static User newClient(String nombre, String correo, String telefono, String passwordHash) {
        return new User(
                null,
                nombre,
                correo,
                telefono,
                passwordHash,
                UserRole.CLIENTE,
                null,
                true,
                true,
                LocalDateTime.now(),
                null
        );
    }
}
