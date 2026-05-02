package io.github.graceman.alicemvc.dto;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Default {@link ResponseFactory} implementation.
 *
 * <p>Wraps all responses in {@link ApiResponse} with the standard envelope:
 * {@code success}, {@code message}, {@code data}, {@code errors},
 * {@code timestamp}.</p>
 *
 * <p>This bean is registered via {@code @ConditionalOnMissingBean} in
 * {@link io.github.graceman.alicemvc.config.AliceMvcAutoConfiguration}.
 * To replace it globally, define your own {@code @Component} implementing
 * {@link ResponseFactory} — this one backs off automatically.</p>
 *
 * @author Graceman
 * @since 1.0.0
 */
public class DefaultResponseFactory implements ResponseFactory {

    @Override
    public <T> ResponseEntity<?> ok(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    @Override
    public <T> ResponseEntity<?> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(data));
    }

    @Override
    public ResponseEntity<?> deleted(String message) {
        return ResponseEntity.ok(ApiResponse.success(null, message));
    }

    @Override
    public <T> ResponseEntity<?> page(Page<T> page, String message) {
        PagedResponse<T> paged = PagedResponse.from(page);
        return ResponseEntity.ok(ApiResponse.success(paged, message));
    }

    @Override
    public ResponseEntity<?> error(String message, Object errors, int status) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(message, errors));
    }
}
