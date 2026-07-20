package com.puravida.shared.web.response;

import java.util.Map;

public record ErrorResponse(String status, String message, Map<String, String> errors, String code) {

    public static ErrorResponse of(String message) {
        return of(message, "ERROR");
    }

    public static ErrorResponse of(String message, String code) {
        return new ErrorResponse("ERROR", message, Map.of(), code);
    }

    public static ErrorResponse validation(Map<String, String> errors) {
        return new ErrorResponse("ERROR", "La solicitud contiene datos invalidos.", errors, "VALIDATION_ERROR");
    }
}
