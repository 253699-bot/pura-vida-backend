package com.puravida.modules.users.application.port.out;

public interface BusinessContactSynchronizationPort {
    void synchronize(String correo, String telefono, Integer actorId);
}
