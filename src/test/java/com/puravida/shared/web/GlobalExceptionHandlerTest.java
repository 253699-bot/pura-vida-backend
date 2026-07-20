package com.puravida.shared.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.puravida.shared.web.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.CannotCreateTransactionException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unexpectedErrorsReturnGenericMessageAndStableCode() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/orders/my");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpectedException(
                new IllegalStateException("detalle interno sensible"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Error interno del servidor.");
        assertThat(response.getBody().message()).doesNotContain("detalle interno sensible");
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void unavailableDatabaseReturnsServiceUnavailableWithoutSqlDetails() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");

        ResponseEntity<ErrorResponse> response = handler.handleDatabaseUnavailable(
                new CannotCreateTransactionException("Access denied for user puravida_app"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("La base de datos no esta disponible.");
        assertThat(response.getBody().message()).doesNotContain("Access denied");
        assertThat(response.getBody().code()).isEqualTo("DATABASE_UNAVAILABLE");
    }
}
