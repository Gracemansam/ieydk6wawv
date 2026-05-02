package io.github.graceman.alicemvc.service;

import io.github.graceman.alicemvc.dto.Identifiable;
import io.github.graceman.alicemvc.event.AliceCrudEvent;
import io.github.graceman.alicemvc.exception.HookCancellationException;
import io.github.graceman.alicemvc.exception.ResourceNotFoundException;
import io.github.graceman.alicemvc.hook.CrudHook;
import io.github.graceman.alicemvc.hook.HookContext;
import io.github.graceman.alicemvc.hook.HookRegistry;
import io.github.graceman.alicemvc.mapper.EntityMapper;
import io.github.graceman.alicemvc.specification.AliceSpecificationBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base service providing CRUD operations with lifecycle hooks,
 * Spring event publishing, and optional DTO mapping.
 *
 * <p><b>Entity-as-DTO mode:</b></p>
 * <pre>{@code
 * @Service
 * public class DepartmentService extends AliceService<Department, Long, Department, Department, Department> {
 *     public DepartmentService(DepartmentRepository repo) { super(repo); }
 * }
 * }</pre>
 *
 * <p><b>Full DTO mode:</b></p>
 * <pre>{@code
 * @Service
 * public class PatientService extends AliceService<Patient, Long, PatientResponse, CreateRequest, UpdateRequest> {
 *     public PatientService(PatientRepository repo, PatientMapper mapper) {
 *         super(repo, mapper);
 *     }
 * }
 * }</pre>
 *
 * @param <T>  entity type
 * @param <ID> primary key type
 * @param <R>  response DTO type
 * @param <C>  create request DTO type
 * @param <U>  update request DTO type
 * @author Graceman
 * @since 1.0.0
 */
public abstract class AliceService<T, ID extends Serializable, R, C, U> {

    private final JpaRepository<T, ID> repository;
    private final EntityMapper<T, R, C, U> mapper;
    private final Class<T> entityClass;

    @Autowired(required = false)
    private HookRegistry hookRegistry;

    @Autowired(required = false)
    private ApplicationEventPublisher eventPublisher;

    /**
     * Constructor for entity-as-DTO mode (no mapper needed).
     *
     * @param repository the JPA repository
     */
    protected AliceService(JpaRepository<T, ID> repository) {
        this.repository = repository;
        this.mapper = null;
        this.entityClass = resolveEntityClass();
    }

    /**
     * Constructor for full DTO mode.
     *
     * @param repository the JPA repository
     * @param mapper     the entity mapper
     */
    protected AliceService(JpaRepository<T, ID> repository, EntityMapper<T, R, C, U> mapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityClass = resolveEntityClass();
    }

    // ========================
    // CRUD Operations
    // ========================

    @Transactional(readOnly = true)
    public Page<R> findAll(Pageable pageable, Map<String, String> filterParams,
                           String search, HttpServletRequest request) {

        // Build specifications
        Specification<T> spec = AliceSpecificationBuilder.buildFromSearchFields(entityClass, filterParams);
        Specification<T> searchSpec = AliceSpecificationBuilder.buildSearchSpec(entityClass, search);
        Specification<T> customSpec = getCustomSpecification(filterParams, request);

        Specification<T> combined = combineSpecs(spec, searchSpec, customSpec);

        Page<T> page;
        if (combined != null && repository instanceof JpaSpecificationExecutor) {
            page = ((JpaSpecificationExecutor<T>) repository).findAll(combined, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public R findById(ID id) {
        T entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityName(), id));
        return mapToResponse(entity);
    }

    @Transactional
    public R create(C createRequest, HttpServletRequest request) {
        T entity = mapFromCreateRequest(createRequest);

        // Before hooks
        HookContext<T, ID> context = new HookContext<>(entity, null,
                HookContext.Action.CREATE, request);
        beforeCreate(context);
        executeExternalHooks(h -> h.beforeCreate(context));
        checkCancellation(context);

        entity = context.getEntity();
        T saved = repository.save(entity);

        // After hooks
        context.setEntity(saved);
        if (saved instanceof Identifiable<?>) {
            @SuppressWarnings("unchecked")
            ID savedId = ((Identifiable<ID>) saved).getId();
            context.setEntityId(savedId);
        }
        afterCreate(context);
        executeExternalHooks(h -> h.afterCreate(context));
        publishEvent(HookContext.Action.CREATE, saved);

        return mapToResponse(saved);
    }

    @Transactional
    public R update(ID id, U updateRequest, HttpServletRequest request) {
        T entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityName(), id));

        applyUpdate(updateRequest, entity);

        // Before hooks
        HookContext<T, ID> context = new HookContext<>(entity, id,
                HookContext.Action.UPDATE, request);
        beforeUpdate(context);
        executeExternalHooks(h -> h.beforeUpdate(context));
        checkCancellation(context);

        entity = context.getEntity();
        T saved = repository.save(entity);

        // After hooks
        context.setEntity(saved);
        afterUpdate(context);
        executeExternalHooks(h -> h.afterUpdate(context));
        publishEvent(HookContext.Action.UPDATE, saved);

        return mapToResponse(saved);
    }

    @Transactional
    public R partialUpdate(ID id, U updateRequest, HttpServletRequest request) {
        T entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityName(), id));

        applyPartialUpdate(updateRequest, entity);

        // Before hooks
        HookContext<T, ID> context = new HookContext<>(entity, id,
                HookContext.Action.PARTIAL_UPDATE, request);
        beforePartialUpdate(context);
        executeExternalHooks(h -> h.beforePartialUpdate(context));
        checkCancellation(context);

        entity = context.getEntity();
        T saved = repository.save(entity);

        // After hooks
        context.setEntity(saved);
        afterPartialUpdate(context);
        executeExternalHooks(h -> h.afterPartialUpdate(context));
        publishEvent(HookContext.Action.PARTIAL_UPDATE, saved);

        return mapToResponse(saved);
    }

    @Transactional
    public void delete(ID id, HttpServletRequest request) {
        T entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityName(), id));

        // Before hooks
        HookContext<T, ID> context = new HookContext<>(entity, id,
                HookContext.Action.DELETE, request);
        beforeDelete(context);
        executeExternalHooks(h -> h.beforeDelete(context));
        checkCancellation(context);

        performDelete(entity, id);

        // After hooks
        afterDelete(context);
        executeExternalHooks(h -> h.afterDelete(context));
        publishEvent(HookContext.Action.DELETE, entity);
    }

    // ════════════════════════════════════════════════════════════════
    //  Inline Hooks — override in your service
    // ════════════════════════════════════════════════════════════════

    /** Override to add logic before create. */
    protected void beforeCreate(HookContext<T, ID> context) {}

    /** Override to add logic after create. */
    protected void afterCreate(HookContext<T, ID> context) {}

    /** Override to add logic before update. */
    protected void beforeUpdate(HookContext<T, ID> context) {}

    /** Override to add logic after update. */
    protected void afterUpdate(HookContext<T, ID> context) {}

    /** Override to add logic before partial update. */
    protected void beforePartialUpdate(HookContext<T, ID> context) {}

    /** Override to add logic after partial update. */
    protected void afterPartialUpdate(HookContext<T, ID> context) {}

    /** Override to add logic before delete. */
    protected void beforeDelete(HookContext<T, ID> context) {}

    /** Override to add logic after delete. */
    protected void afterDelete(HookContext<T, ID> context) {}

    // ════════════════════════════════════════════════════════════════
    //  Override Points
    // ════════════════════════════════════════════════════════════════

    /**
     * Override to implement soft delete instead of hard delete.
     *
     * @param entity the entity to delete
     * @param id     the entity ID
     */
    protected void performDelete(T entity, ID id) {
        repository.deleteById(id);
    }

    /**
     * Override to add custom JPA Specifications beyond @SearchField.
     *
     * @param filters the filter parameters
     * @param request the HTTP request
     * @return custom specification, or null
     */
    protected Specification<T> getCustomSpecification(
            Map<String, String> filters, HttpServletRequest request) {
        return null;
    }

    // ════════════════════════════════════════════════════════════════
    //  Mapping
    // ════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    protected R mapToResponse(T entity) {
        if (mapper != null) return mapper.toResponse(entity);
        return (R) entity; // entity-as-DTO mode
    }

    @SuppressWarnings("unchecked")
    protected T mapFromCreateRequest(C createRequest) {
        if (mapper != null) return mapper.fromCreateRequest(createRequest);
        return (T) createRequest; // entity-as-DTO mode
    }

    protected void applyUpdate(U updateRequest, T entity) {
        if (mapper != null) {
            mapper.applyUpdate(updateRequest, entity);
        }
        // In entity-as-DTO mode, the controller sends the full entity
    }

    protected void applyPartialUpdate(U updateRequest, T entity) {
        if (mapper != null) {
            mapper.applyPartialUpdate(updateRequest, entity);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  Internal
    // ════════════════════════════════════════════════════════════════

    private void executeExternalHooks(Consumer<CrudHook<T, ID>> hookAction) {
        if (hookRegistry == null) return;
        List<CrudHook<T, ID>> hooks = hookRegistry.getHooksFor(entityClass);
        hooks.forEach(hookAction);
    }

    private void checkCancellation(HookContext<T, ID> context) {
        if (context.isCancelled()) {
            throw new HookCancellationException(context.getCancellationReason());
        }
    }

    private void publishEvent(HookContext.Action action, T entity) {
        if (eventPublisher == null) return;
        Object id = (entity instanceof Identifiable<?>) ? ((Identifiable<?>) entity).getId() : null;
        eventPublisher.publishEvent(new AliceCrudEvent<>(this, entity, id, action));
    }

    @SafeVarargs
    private Specification<T> combineSpecs(Specification<T>... specs) {
        Specification<T> combined = null;
        for (Specification<T> spec : specs) {
            if (spec == null) continue;
            combined = (combined == null) ? spec : combined.and(spec);
        }
        return combined;
    }

    public String getEntityName() {
        return entityClass != null ? entityClass.getSimpleName() : "Resource";
    }

    protected JpaRepository<T, ID> getRepository() {
        return repository;
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }

    @SuppressWarnings("unchecked")
    private Class<T> resolveEntityClass() {
        Class<?> current = getClass();

        while (current != null && current != Object.class) {
            Type genericSuper = current.getGenericSuperclass();

            if (genericSuper instanceof ParameterizedType pt) {
                Class<?> rawType = (Class<?>) pt.getRawType();

                // Check if rawType is AliceService or any class between
                // the concrete service and AliceService (e.g. SimpleAliceService).
                // The first type argument is always the entity class.
                if (AliceService.class.isAssignableFrom(rawType)) {
                    Type entityType = pt.getActualTypeArguments()[0];
                    if (entityType instanceof Class<?>) {
                        return (Class<T>) entityType;
                    }
                    // If it's still a TypeVariable (e.g. T), keep walking up
                }
            }

            current = current.getSuperclass();
        }

        throw new IllegalStateException(
                "Cannot resolve entity class for AliceService. " +
                "Make sure your service extends SimpleAliceService<Entity, ID> or " +
                "AliceService<Entity, ID, R, C, U> with concrete type arguments.");
    }
}
