package io.github.graceman.alicemvc.mapper;

/**
 * Contract for mapping between entities and DTOs.
 *
 * <p>Implement this when using separate create/update request and
 * response DTOs. For entity-as-DTO mode, skip this entirely.</p>
 *
 * @param <T> entity type
 * @param <R> response DTO type
 * @param <C> create request DTO type
 * @param <U> update request DTO type
 * @author Graceman
 * @since 1.0.0
 */
public interface EntityMapper<T, R, C, U> {

    /**
     * Convert entity to response DTO.
     *
     * @param entity the entity
     * @return the response DTO
     */
    R toResponse(T entity);

    /**
     * Convert create request DTO to entity.
     *
     * @param createRequest the create request
     * @return the entity
     */
    T fromCreateRequest(C createRequest);

    /**
     * Apply update request DTO fields onto an existing entity.
     *
     * @param updateRequest the update request
     * @param entity        the existing entity
     */
    void applyUpdate(U updateRequest, T entity);

    /**
     * Apply partial update — only non-null fields from the request.
     * Default implementation delegates to {@link #applyUpdate(Object, Object)}.
     *
     * @param updateRequest the partial update request
     * @param entity        the existing entity
     */
    default void applyPartialUpdate(U updateRequest, T entity) {
        applyUpdate(updateRequest, entity);
    }
}
