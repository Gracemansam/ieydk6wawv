package io.github.graceman.alicemvc.dto;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Contract for building HTTP responses throughout AliceMVC.
 *
 * <p>Every controller endpoint delegates response construction to this
 * factory. Replace the default implementation to change the response
 * envelope, field names, status codes, or structure globally — without
 * touching any controller code.</p>
 *
 * <p><b>Default behavior:</b> The built-in {@link DefaultResponseFactory}
 * wraps everything in {@link ApiResponse} with fields:
 * {@code success}, {@code message}, {@code data}, {@code errors},
 * {@code timestamp}.</p>
 *
 * <p><b>To customize globally:</b> Define your own {@code @Component}
 * implementing {@code ResponseFactory}. The default backs off
 * automatically via {@code @ConditionalOnMissingBean}.</p>
 *
 * <p><b>Example — flat response (no envelope):</b></p>
 * <pre>{@code
 * @Component
 * public class FlatResponseFactory implements ResponseFactory {
 *     public <T> ResponseEntity<?> ok(T data, String message) {
 *         return ResponseEntity.ok(data);
 *     }
 *     public <T> ResponseEntity<?> created(T data, String message) {
 *         return ResponseEntity.status(201).body(data);
 *     }
 *     public ResponseEntity<?> deleted(String message) {
 *         return ResponseEntity.noContent().build();
 *     }
 *     public <T> ResponseEntity<?> page(Page<T> page, String message) {
 *         return ResponseEntity.ok(page);
 *     }
 *     public ResponseEntity<?> error(String message, Object errors, int status) {
 *         return ResponseEntity.status(status).body(Map.of("error", message));
 *     }
 * }
 * }</pre>
 *

 * @since 1.0.0
 */
public interface ResponseFactory {

    /**
     * Build a 200 OK response.
     *
     * @param data    the response payload
     * @param message a human-readable message
     * @param <T>     payload type
     * @return the HTTP response
     */
    <T> ResponseEntity<?> ok(T data, String message);

    /**
     * Build a 201 Created response.
     *
     * @param data    the created resource
     * @param message a human-readable message
     * @param <T>     payload type
     * @return the HTTP response
     */
    <T> ResponseEntity<?> created(T data, String message);

    /**
     * Build a response for a successful delete operation.
     *
     * @param message a human-readable message
     * @return the HTTP response
     */
    ResponseEntity<?> deleted(String message);

    /**
     * Build a response for a paginated list.
     *
     * @param page    the Spring Data Page
     * @param message a human-readable message
     * @param <T>     payload type
     * @return the HTTP response
     */
    <T> ResponseEntity<?> page(Page<T> page, String message);

    /**
     * Build an error response.
     *
     * @param message a human-readable error message
     * @param errors  structured error details (may be null)
     * @param status  the HTTP status code
     * @return the HTTP response
     */
    ResponseEntity<?> error(String message, Object errors, int status);
}
