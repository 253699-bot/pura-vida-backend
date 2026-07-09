package com.puravida.modules.auth.domain.exception;

import com.puravida.shared.domain.exception.PuraVidaException;

public class EmailAlreadyRegisteredException extends PuraVidaException {

    public EmailAlreadyRegisteredException() {
        super("El correo ya esta registrado.");
    }
}
