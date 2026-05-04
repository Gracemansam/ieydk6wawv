package io.github.graceman.alicemvc.annotation;

import java.lang.annotation.*;

/**
 * Restricts CRUD operations based on Spring Security granted authorities.
 *
 * <p>Works with <b>any</b> type of authority string that Spring Security
 * puts in {@code SecurityContextHolder} — roles, permissions, scopes,
 * or custom authority strings. All of these work:</p>
 *
 * <ul>
 *     <li><b>Roles:</b> {@code "ADMIN"}, {@code "ROLE_ADMIN"}</li>
 *     <li><b>Permissions:</b> {@code "patient:create"}, {@code "order:delete"}</li>
 *     <li><b>OAuth2 scopes:</b> {@code "SCOPE_read"}, {@code "SCOPE_write"}</li>
 *     <li><b>Custom:</b> any string your app puts in granted authorities</li>
 * </ul>
 *
 * <p><b>Important:</b> AliceMVC does NOT depend on Spring Security.
 * If Spring Security is not on the classpath, this annotation is silently
 * ignored and all operations remain open. No conflicts, no forced dependencies.</p>
 *
 * <p><b>Matching behavior:</b></p>
 * <ul>
 *     <li>By default, the user needs <b>at least one</b> of the listed
 *         authorities to access the operation (OR logic)</li>
 *     <li>Set {@code matchAll = true} to require <b>all</b> listed
 *         authorities (AND logic)</li>
 *     <li>For role-style strings, the framework checks both formats:
 *         {@code "ADMIN"} matches authorities {@code ADMIN} and {@code ROLE_ADMIN}</li>
 *     <li>For permission-style strings like {@code "patient:create"},
 *         the framework matches them exactly as-is</li>
 * </ul>
 *
 * <p><b>Example — using roles:</b></p>
 * <pre>{@code
 * @AlicePermission(
 *     list     = {"USER", "ADMIN"},
 *     create   = {"ADMIN", "DOCTOR"},
 *     delete   = {"SUPER_ADMIN"}
 * )
 * }</pre>
 *
 * <p><b>Example — using permission strings:</b></p>
 * <pre>{@code
 * @AlicePermission(
 *     list     = {"patient:read"},
 *     create   = {"patient:create"},
 *     update   = {"patient:update"},
 *     delete   = {"patient:delete"}
 * )
 * }</pre>
 *
 * <p><b>Example — mixing roles and permissions:</b></p>
 * <pre>{@code
 * @AlicePermission(
 *     list     = {"USER", "patient:read"},
 *     create   = {"ADMIN", "patient:create"},
 *     delete   = {"SUPER_ADMIN"}
 * )
 * }</pre>
 *
 * <p><b>Example — require ALL authorities (AND logic):</b></p>
 * <pre>{@code
 * @AlicePermission(
 *     create   = {"ADMIN", "VERIFIED"},
 *     matchAll = true
 *     // user must have BOTH ADMIN and VERIFIED to create
 * )
 * }</pre>
 *
 * @author Graceman — In loving memory of Grandma Alice
 * @since 1.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AlicePermission {

    /**
     * Authorities allowed to access the LIST (GET /) endpoint.
     * Empty = open to all.
     */
    String[] list() default {};

    /**
     * Authorities allowed to access the RETRIEVE (GET /{id}) endpoint.
     * Empty = open to all.
     */
    String[] retrieve() default {};

    /**
     * Authorities allowed to access the CREATE (POST /) endpoint.
     * Empty = open to all.
     */
    String[] create() default {};

    /**
     * Authorities allowed to access the UPDATE (PUT /{id}) endpoint.
     * Empty = open to all.
     */
    String[] update() default {};

    /**
     * Authorities allowed to access the PARTIAL_UPDATE (PATCH /{id}) endpoint.
     * Empty = open to all.
     */
    String[] partialUpdate() default {};

    /**
     * Authorities allowed to access the DELETE (DELETE /{id}) endpoint.
     * Empty = open to all.
     */
    String[] delete() default {};

    /**
     * Match mode for authorities.
     *
     * <p>{@code false} (default) = user needs <b>at least one</b> of
     * the listed authorities (OR logic).</p>
     * <p>{@code true} = user needs <b>all</b> of the listed
     * authorities (AND logic).</p>
     *
     * @return true for AND matching, false for OR matching
     */
    boolean matchAll() default false;
}
