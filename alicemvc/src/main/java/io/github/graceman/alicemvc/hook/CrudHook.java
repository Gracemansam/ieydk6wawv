package io.github.graceman.alicemvc.hook;

import java.io.Serializable;

/**
 * Standalone lifecycle hook for CRUD operations.
 *
 * <p>Implement this interface as a {@code @Component} to hook into any
 * entity's CRUD lifecycle without modifying the entity's service.</p>
 *
 * <p><b>Example — audit logging hook:</b></p>
 * <pre>{@code
 * @Component
 * public class OrderAuditHook implements CrudHook<Order, Long> {
 *
 *     @Override
 *     public Class<Order> getEntityClass() { return Order.class; }
 *
 *     @Override
 *     public void beforeCreate(HookContext<Order, Long> context) {
 *         // validate, enrich, or cancel
 *     }
 *
 *     @Override
 *     public void afterCreate(HookContext<Order, Long> context) {
 *         // audit log, notify, sync
 *     }
 * }
 * }</pre>
 *
 * @param <T>  entity type
 * @param <ID> primary key type
 * @author Graceman
 * @since 1.0.0
 */
public interface CrudHook<T, ID extends Serializable> {

    /**
     * The entity class this hook targets.
     *
     * @return the entity class
     */
    Class<T> getEntityClass();

    /**
     * Called before a new entity is persisted.
     *
     * @param context the hook context
     */
    default void beforeCreate(HookContext<T, ID> context) {}

    /**
     * Called after a new entity is persisted.
     *
     * @param context the hook context
     */
    default void afterCreate(HookContext<T, ID> context) {}

    /**
     * Called before an entity is fully updated (PUT).
     *
     * @param context the hook context
     */
    default void beforeUpdate(HookContext<T, ID> context) {}

    /**
     * Called after an entity is fully updated (PUT).
     *
     * @param context the hook context
     */
    default void afterUpdate(HookContext<T, ID> context) {}

    /**
     * Called before an entity is partially updated (PATCH).
     *
     * @param context the hook context
     */
    default void beforePartialUpdate(HookContext<T, ID> context) {}

    /**
     * Called after an entity is partially updated (PATCH).
     *
     * @param context the hook context
     */
    default void afterPartialUpdate(HookContext<T, ID> context) {}

    /**
     * Called before an entity is deleted.
     *
     * @param context the hook context
     */
    default void beforeDelete(HookContext<T, ID> context) {}

    /**
     * Called after an entity is deleted.
     *
     * @param context the hook context
     */
    default void afterDelete(HookContext<T, ID> context) {}
}
