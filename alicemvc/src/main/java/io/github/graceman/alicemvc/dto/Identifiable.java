package io.github.graceman.alicemvc.dto;

/**
 * Contract for entities that expose their primary key.
 *
 * @param <ID> the primary key type
 * @author Graceman
 * @since 1.0.0
 */
public interface Identifiable<ID> {
    ID getId();
}
