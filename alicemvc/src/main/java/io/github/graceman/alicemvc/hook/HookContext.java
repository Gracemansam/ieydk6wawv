package io.github.graceman.alicemvc.hook;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Context object passed to lifecycle hooks.
 *
 * <p>Carries the entity, its ID, the action being performed, the HTTP request,
 * and a map of custom attributes that can be passed between before and after hooks.</p>
 *
 * @param <T>  entity type
 * @param <ID> primary key type
 * @author Graceman
 * @since 1.0.0
 */
public class HookContext<T, ID extends Serializable> {

    public enum Action {
        CREATE, UPDATE, PARTIAL_UPDATE, DELETE
    }

    private T entity;
    private ID entityId;
    private Action action;
    private HttpServletRequest request;
    private boolean cancelled;
    private String cancellationReason;
    private final Map<String, Object> attributes = new HashMap<>();

    public HookContext(T entity, ID entityId, Action action, HttpServletRequest request) {
        this.entity = entity;
        this.entityId = entityId;
        this.action = action;
        this.request = request;
    }

    /**
     * Cancel the current operation. The entity will NOT be saved.
     *
     * @param reason human-readable reason for cancellation
     */
    public void cancel(String reason) {
        this.cancelled = true;
        this.cancellationReason = reason;
    }

    /**
     * Store a custom attribute to pass data between before and after hooks.
     *
     * @param key   the attribute key
     * @param value the attribute value
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Retrieve a custom attribute set by a previous hook.
     *
     * @param key the attribute key
     * @return the attribute value, or null
     */
    @SuppressWarnings("unchecked")
    public <V> V getAttribute(String key) {
        return (V) attributes.get(key);
    }

    // --- Getters and Setters ---

    public T getEntity() { return entity; }
    public void setEntity(T entity) { this.entity = entity; }
    public ID getEntityId() { return entityId; }
    public void setEntityId(ID entityId) { this.entityId = entityId; }
    public Action getAction() { return action; }
    public HttpServletRequest getRequest() { return request; }
    public boolean isCancelled() { return cancelled; }
    public String getCancellationReason() { return cancellationReason; }
    public Map<String, Object> getAttributes() { return attributes; }
}
