package io.github.graceman.alicemvc.exception;

/**
 * Thrown when a lifecycle hook cancels a CRUD operation.
 *

 * @since 1.0.0
 */
public class HookCancellationException extends RuntimeException {

    public HookCancellationException(String reason) {
        super(reason);
    }
}
