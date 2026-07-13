package com.puravida.modules.cart.domain.exception;

import com.puravida.shared.domain.exception.PuraVidaException;

public class CartValidationException extends PuraVidaException {

    public CartValidationException(String message) {
        super(message);
    }
}
