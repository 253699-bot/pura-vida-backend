package com.puravida.modules.businessconfiguration.domain.exception;

import com.puravida.shared.domain.exception.PuraVidaException;

public class BusinessConfigurationValidationException extends PuraVidaException {
    public BusinessConfigurationValidationException(String message) {
        super(message);
    }
}
