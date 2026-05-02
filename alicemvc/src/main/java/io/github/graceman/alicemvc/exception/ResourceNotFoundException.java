package io.github.graceman.alicemvc.exception;

/**
 * Thrown when a requested resource is not found (404).
 *

 * @since 1.0.0
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("%s not found with id: %s", resourceName, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
