package io.github.graceman.alicemvc.hook;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry that auto-discovers {@link CrudHook} beans and groups them
 * by entity class for fast dispatch.
 *
 * @author Graceman
 * @since 1.0.0
 */
@Component
public class HookRegistry {

    private final Map<Class<?>, List<CrudHook<?, ?>>> hooksByEntity = new ConcurrentHashMap<>();

    public HookRegistry(List<CrudHook<?, ?>> hooks) {
        if (hooks != null) {
            hooksByEntity.putAll(
                    hooks.stream().collect(Collectors.groupingBy(CrudHook::getEntityClass))
            );
        }
    }

    /**
     * Get all hooks registered for a given entity class.
     *
     * @param entityClass the entity class
     * @param <T>         entity type
     * @param <ID>        primary key type
     * @return list of hooks (never null)
     */
    @SuppressWarnings("unchecked")
    public <T, ID extends Serializable> List<CrudHook<T, ID>> getHooksFor(Class<T> entityClass) {
        List<CrudHook<?, ?>> hooks = hooksByEntity.getOrDefault(entityClass, Collections.emptyList());
        return hooks.stream()
                .map(h -> (CrudHook<T, ID>) h)
                .collect(Collectors.toList());
    }
}
