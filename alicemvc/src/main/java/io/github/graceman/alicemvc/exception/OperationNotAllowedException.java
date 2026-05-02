package io.github.graceman.alicemvc.exception;

/**
 * Thrown when a disabled operation is attempted (405).
 *

 * @since 1.0.0
 */
public class OperationNotAllowedException extends RuntimeException {

    public OperationNotAllowedException(String operation, String resourceName) {
        super(String.format("'%s' operation is not allowed on %s", operation, resourceName));
    }
}
