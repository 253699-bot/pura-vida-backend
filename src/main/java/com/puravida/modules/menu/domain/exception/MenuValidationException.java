package com.puravida.modules.menu.domain.exception;

import com.puravida.shared.domain.exception.PuraVidaException;

public class MenuValidationException extends PuraVidaException {

    public MenuValidationException(String message) {
        super(message);
    }
}
