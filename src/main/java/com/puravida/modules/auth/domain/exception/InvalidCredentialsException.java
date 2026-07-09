package com.puravida.modules.auth.domain.exception;

import com.puravida.shared.domain.exception.PuraVidaException;

public class InvalidCredentialsException extends PuraVidaException {

    public InvalidCredentialsException() {
        super("Credenciales invalidas.");
    }
}
