package com.puravida.modules.dashboard.domain.exception;

import com.puravida.shared.domain.exception.PuraVidaException;

public class DashboardValidationException extends PuraVidaException {

    public DashboardValidationException(String message) {
        super(message);
    }
}
