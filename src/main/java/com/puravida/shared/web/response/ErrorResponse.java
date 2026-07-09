package com.puravida.shared.web.response;

import java.util.Map;

public record ErrorResponse(String status, String message, Map<String, String> errors) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse("ERROR", message, Map.of());
    }

    public static ErrorResponse validation(Map<String, String> errors) {
        return new ErrorResponse("ERROR", "La solicitud contiene datos invalidos.", errors);
    }
}
