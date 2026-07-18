package com.puravida.modules.users.domain.exception;

import com.puravida.shared.domain.exception.PuraVidaException;

public class UserProfileValidationException extends PuraVidaException {

    public UserProfileValidationException(String message) {
        super(message);
    }
}
