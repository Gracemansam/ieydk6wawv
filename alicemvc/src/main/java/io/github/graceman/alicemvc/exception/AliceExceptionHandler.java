package io.github.graceman.alicemvc.exception;

import io.github.graceman.alicemvc.dto.ResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for AliceMVC.
 *
 * <p>Catches validation errors, not-found, hook cancellations, business
 * rule violations, and unexpected exceptions. Uses {@link ResponseFactory}
 * so error responses match whatever format the developer chose.</p>
 *
 * <p><b>Enabled by default</b>, but completely optional:</p>
 *
 * <p><b>Option 1 — Disable via property:</b></p>
 * <pre>
 * # application.properties
 * alicemvc.exception-handler.enabled=false
 * </pre>
 *
 * <p><b>Option 2 — Replace by extending:</b></p>
 * <pre>{@code
 * @RestControllerAdvice
 * public class MyExceptionHandler extends AliceExceptionHandler {
 *     public MyExceptionHandler(ResponseFactory responseFactory) {
 *         super(responseFactory);
 *     }
 *     // override any method
 * }
 * }</pre>
 *
 * <p><b>Option 3 — Write your own from scratch:</b> disable via property
 * and define your own {@code @RestControllerAdvice}.</p>
 *
 * @author Graceman — In loving memory of Grandma Alice
 * @since 1.0.0
 */
@RestControllerAdvice
public class AliceExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AliceExceptionHandler.class);

    private final ResponseFactory responseFactory;

    public AliceExceptionHandler(ResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return responseFactory.error(ex.getMessage(), null, HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(HookCancellationException.class)
    public ResponseEntity<?> handleHookCancellation(HookCancellationException ex) {
        return responseFactory.error(ex.getMessage(), null, HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException ex) {
        return responseFactory.error(ex.getMessage(), null, HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<?> handleNotAllowed(OperationNotAllowedException ex) {
        return responseFactory.error(ex.getMessage(), null, HttpStatus.METHOD_NOT_ALLOWED.value());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return responseFactory.error(ex.getMessage(), null, HttpStatus.FORBIDDEN.value());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(formatFieldError(error.getField(), error.getDefaultMessage()));
        }
        return responseFactory.error("Validation failed", errors, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return responseFactory.error(ex.getMessage(), null, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        log.error("AliceMVC: Unhandled exception", ex);
        return responseFactory.error("An unexpected error occurred", null,
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * Format a field validation error. Override to change the format.
     *
     * @param fieldName    the field that failed validation
     * @param errorMessage the validation error message
     * @return the formatted error string
     */
    protected String formatFieldError(String fieldName, String errorMessage) {
        return fieldName + ": " + errorMessage;
    }
}
