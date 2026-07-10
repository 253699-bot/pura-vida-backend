package com.puravida.modules.business.domain.exception;

import com.puravida.shared.domain.exception.PuraVidaException;

public class BusinessStatusValidationException extends PuraVidaException {

    public BusinessStatusValidationException(String message) {
        super(message);
    }
}
