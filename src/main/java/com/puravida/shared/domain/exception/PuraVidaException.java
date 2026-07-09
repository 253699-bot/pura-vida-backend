package com.puravida.shared.domain.exception;

public class PuraVidaException extends RuntimeException {

    public PuraVidaException(String message) {
        super(message);
    }

    public PuraVidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
