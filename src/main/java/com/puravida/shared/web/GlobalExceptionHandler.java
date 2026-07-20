package com.puravida.shared.web;

import com.puravida.modules.auth.domain.exception.EmailAlreadyRegisteredException;
import com.puravida.modules.auth.domain.exception.InactiveUserException;
import com.puravida.modules.auth.domain.exception.InvalidCredentialsException;
import com.puravida.modules.business.domain.exception.ActiveOrdersPreventClosureException;
import com.puravida.shared.domain.exception.ConflictException;
import com.puravida.shared.domain.exception.ForbiddenException;
import com.puravida.shared.domain.exception.NotFoundException;
import com.puravida.shared.domain.exception.PuraVidaException;
import com.puravida.shared.domain.exception.UnauthorizedException;
import com.puravida.shared.web.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(ErrorResponse.validation(errors));
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(exception.getMessage(), "EMAIL_ALREADY_EXISTS"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(exception.getMessage(), "INVALID_CREDENTIALS"));
    }

    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<ErrorResponse> handleInactiveUser(InactiveUserException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(exception.getMessage(), "USER_INACTIVE"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(exception.getMessage(), "UNAUTHORIZED"));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(exception.getMessage(), "FORBIDDEN"));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(exception.getMessage(), "RESOURCE_NOT_FOUND"));
    }

    @ExceptionHandler(ActiveOrdersPreventClosureException.class)
    public ResponseEntity<ErrorResponse> handleActiveOrdersPreventClosure(
            ActiveOrdersPreventClosureException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "ERROR",
                        exception.getMessage(),
                        Map.of(
                                "pendientes", String.valueOf(exception.counts().pending()),
                                "aceptados", String.valueOf(exception.counts().accepted())
                        ),
                        "ACTIVE_ORDERS_PREVENT_CLOSURE"
                ));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(exception.getMessage(), "CONFLICT"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        "No se puede completar la operacion por restricciones de integridad.",
                        "DATA_INTEGRITY_CONFLICT"
                ));
    }

    @ExceptionHandler({
            DataAccessResourceFailureException.class,
            CannotCreateTransactionException.class
    })
    public ResponseEntity<ErrorResponse> handleDatabaseUnavailable(
            Exception exception,
            HttpServletRequest request
    ) {
        String errorId = UUID.randomUUID().toString();
        log.error("Database unavailable errorId={} method={} path={} type={} rootCauseType={}",
                errorId,
                request.getMethod(),
                request.getRequestURI(),
                exception.getClass().getName(),
                rootCauseType(exception)
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of("La base de datos no esta disponible.", "DATABASE_UNAVAILABLE"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("El body de la solicitud no es valido.", "INVALID_REQUEST_BODY"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("Uno de los parametros de la solicitud no es valido.", "INVALID_REQUEST"));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestPart(MissingServletRequestPartException exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("Falta una parte requerida de la solicitud.", "INVALID_REQUEST"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("El archivo excede el tamano maximo permitido.", "FILE_TOO_LARGE"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipart(MultipartException exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("La solicitud multipart no es valida.", "INVALID_REQUEST"));
    }

    @ExceptionHandler(PuraVidaException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(PuraVidaException exception) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(exception.getMessage(), "VALIDATION_ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unexpected server errorId={} method={} path={} type={} rootCauseType={}",
                errorId,
                request.getMethod(),
                request.getRequestURI(),
                exception.getClass().getName(),
                rootCauseType(exception)
        );
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of("Error interno del servidor.", "INTERNAL_ERROR"));
    }

    private static String rootCauseType(Throwable exception) {
        Throwable current = exception;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getClass().getName();
    }
}