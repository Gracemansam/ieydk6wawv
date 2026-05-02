package io.github.graceman.alicemvc.event;

import io.github.graceman.alicemvc.hook.HookContext;
import org.springframework.context.ApplicationEvent;

/**
 * Spring event published after every successful CRUD operation.
 *
 * <p>Listen with {@code @EventListener} for fully decoupled event handling:</p>
 * <pre>{@code
 * @Component
 * public class CrudEventLogger {
 *
 *     @EventListener
 *     public void onCrudEvent(AliceCrudEvent<?> event) {
 *         log.info("{} on {} (ID: {})",
 *                 event.getAction(),
 *                 event.getEntityClass().getSimpleName(),
 *                 event.getEntityId());
 *     }
 *
 *     @EventListener(condition = "#event.action.name() == 'CREATE'")
 *     public void onCreated(AliceCrudEvent<?> event) {
 *         // send notification, sync to external system, etc.
 *     }
 * }
 * }</pre>
 *
 * @param <T> entity type

 * @since 1.0.0
 */
public class AliceCrudEvent<T> extends ApplicationEvent {

    private final T entity;
    private final Object entityId;
    private final Class<T> entityClass;
    private final HookContext.Action action;

    @SuppressWarnings("unchecked")
    public AliceCrudEvent(Object source, T entity, Object entityId, HookContext.Action action) {
        super(source);
        this.entity = entity;
        this.entityId = entityId;
        this.entityClass = (Class<T>) entity.getClass();
        this.action = action;
    }

    public T getEntity() { return entity; }
    public Object getEntityId() { return entityId; }
    public Class<T> getEntityClass() { return entityClass; }
    public HookContext.Action getAction() { return action; }
}
