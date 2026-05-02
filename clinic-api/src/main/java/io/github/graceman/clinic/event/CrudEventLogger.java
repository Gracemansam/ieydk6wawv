package io.github.graceman.clinic.event;

import io.github.graceman.alicemvc.event.AliceCrudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * DEMO: Fully decoupled CRUD event listener.
 * Third way to hook into CRUD (alongside inline hooks and CrudHook beans).
 */
@Component
public class CrudEventLogger {

    private static final Logger log = LoggerFactory.getLogger(CrudEventLogger.class);

    @EventListener
    public void onCrudEvent(AliceCrudEvent<?> event) {
        log.info("[EVENT] {} on {} (ID: {})",
                event.getAction(),
                event.getEntityClass().getSimpleName(),
                event.getEntityId());
    }

    @EventListener(condition = "#event.action.name() == 'CREATE'")
    public void onCreated(AliceCrudEvent<?> event) {
        log.info("[EVENT] New {} created with ID: {}",
                event.getEntityClass().getSimpleName(),
                event.getEntityId());
    }
}
