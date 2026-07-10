package com.puravida.modules.orders.domain.exception;

import com.puravida.shared.domain.exception.PuraVidaException;

public class OrderValidationException extends PuraVidaException {

    public OrderValidationException(String message) {
        super(message);
    }
}
