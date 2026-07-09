package com.puravida.modules.auth.domain.exception;

import com.puravida.shared.domain.exception.PuraVidaException;

public class InactiveUserException extends PuraVidaException {

    public InactiveUserException() {
        super("El usuario se encuentra inactivo.");
    }
}
