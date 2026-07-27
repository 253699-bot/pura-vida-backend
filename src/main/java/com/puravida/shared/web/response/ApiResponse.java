// Crea una respuesta exitosa con el formato estándar.
package com.puravida.shared.web.response;

public record ApiResponse<T>(String status, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("OK", data);
    }
}
