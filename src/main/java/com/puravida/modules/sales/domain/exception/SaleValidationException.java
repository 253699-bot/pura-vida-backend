package com.puravida.modules.sales.domain.exception;

import com.puravida.shared.domain.exception.PuraVidaException;

public class SaleValidationException extends PuraVidaException {

    public SaleValidationException(String message) {
        super(message);
    }
}
