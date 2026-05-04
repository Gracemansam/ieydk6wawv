package io.github.graceman.alicemvc.exception;

/**
 * Thrown when a user lacks permission for a CRUD operation.
 *
 * <p>Mapped to HTTP 403 (Forbidden) by the exception handler.</p>
 *
 * @author Graceman — In loving memory of Grandma Alice
 * @since 1.1.0
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
