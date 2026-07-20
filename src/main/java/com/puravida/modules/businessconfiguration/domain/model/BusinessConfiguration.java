package com.puravida.modules.businessconfiguration.domain.model;

import java.time.LocalDateTime;

public record BusinessConfiguration(
        Integer id,
        String nombreFonda,
        String logoKey,
        String direccion,
        String horarios,
        String telefono,
        String correo,
        Integer actualizadoPor,
        LocalDateTime actualizadoEn
) {
    public BusinessConfiguration updateDetails(
            String nombreFonda,
            String direccion,
            String horarios,
            String telefono,
            String correo,
            Integer actorId
    ) {
        return new BusinessConfiguration(
                id, nombreFonda, logoKey, direccion, horarios, telefono, correo,
                actorId, LocalDateTime.now()
        );
    }

    public BusinessConfiguration updateLogo(String newLogoKey, Integer actorId) {
        return new BusinessConfiguration(
                id, nombreFonda, newLogoKey, direccion, horarios, telefono, correo,
                actorId, LocalDateTime.now()
        );
    }

    public BusinessConfiguration synchronizeContact(String newCorreo, String newTelefono, Integer actorId) {
        return new BusinessConfiguration(
                id, nombreFonda, logoKey, direccion, horarios, newTelefono, newCorreo,
                actorId, LocalDateTime.now()
        );
    }
}
