package io.github.graceman.alicemvc.security;

import io.github.graceman.alicemvc.annotation.AlicePermission;
import io.github.graceman.alicemvc.annotation.DisableOperation.Operation;

import java.util.Collection;

/**
 * Checks whether the current user has permission to perform a CRUD operation
 * based on {@link AlicePermission} annotation.
 *
 * <p>This class uses <b>reflection</b> to access Spring Security's
 * {@code SecurityContextHolder}. Spring Security is NOT a dependency of
 * AliceMVC — it is only accessed at runtime if present on the classpath.
 * If Spring Security is absent, all operations are permitted.</p>
 *
 * <p>Supports any authority string: roles ({@code ADMIN}, {@code ROLE_ADMIN}),
 * permissions ({@code patient:create}), OAuth2 scopes ({@code SCOPE_read}),
 * or custom strings.</p>
 *
 * @author Graceman — In loving memory of Grandma Alice
 * @since 1.1.0
 */
public class AlicePermissionChecker {

    private static final String WILDCARD = "*";
    private static final Boolean SPRING_SECURITY_AVAILABLE = checkSpringSecurityAvailable();

    /**
     * Check if the current user is allowed to perform the given operation.
     *
     * @param controllerClass the controller class (checked for @AlicePermission)
     * @param operation       the CRUD operation being attempted
     * @throws io.github.graceman.alicemvc.exception.AccessDeniedException if the user lacks the required authority
     */
    public static void checkPermission(Class<?> controllerClass, Operation operation) {
        AlicePermission permission = controllerClass.getAnnotation(AlicePermission.class);
        if (permission == null) {
            return; // No annotation — all operations open
        }

        String[] requiredAuthorities = getAuthoritiesForOperation(permission, operation);
        if (requiredAuthorities == null || requiredAuthorities.length == 0) {
            return; // No authorities specified for this operation — open to all
        }

        // Check for wildcard
        for (String auth : requiredAuthorities) {
            if (WILDCARD.equals(auth)) {
                return; // Wildcard = open to everyone
            }
        }

        // If Spring Security is not on the classpath, skip check
        if (!SPRING_SECURITY_AVAILABLE) {
            return;
        }

        // Get current user's authorities
        Collection<String> userAuthorities = getCurrentUserAuthorities();
        if (userAuthorities == null || userAuthorities.isEmpty()) {
            throw new io.github.graceman.alicemvc.exception.AccessDeniedException(
                    "Authentication required for " + operation.name() + " operation");
        }

        boolean matchAll = permission.matchAll();

        if (matchAll) {
            // AND logic — user must have ALL listed authorities
            checkMatchAll(requiredAuthorities, userAuthorities, operation);
        } else {
            // OR logic — user must have AT LEAST ONE listed authority
            checkMatchAny(requiredAuthorities, userAuthorities, operation);
        }
    }

    /**
     * OR logic — user needs at least one of the required authorities.
     */
    private static void checkMatchAny(String[] requiredAuthorities,
                                       Collection<String> userAuthorities,
                                       Operation operation) {
        for (String required : requiredAuthorities) {
            if (hasAuthority(userAuthorities, required)) {
                return; // Found a match — access granted
            }
        }

        throw new io.github.graceman.alicemvc.exception.AccessDeniedException(
                "Access denied: requires one of [" + String.join(", ", requiredAuthorities) +
                "] for " + operation.name() + " operation");
    }

    /**
     * AND logic — user needs all of the required authorities.
     */
    private static void checkMatchAll(String[] requiredAuthorities,
                                       Collection<String> userAuthorities,
                                       Operation operation) {
        for (String required : requiredAuthorities) {
            if (!hasAuthority(userAuthorities, required)) {
                throw new io.github.graceman.alicemvc.exception.AccessDeniedException(
                        "Access denied: requires all of [" + String.join(", ", requiredAuthorities) +
                        "] for " + operation.name() + " operation");
            }
        }
    }

    /**
     * Check if the user has a specific authority.
     * Handles multiple formats:
     *   - Exact match: "patient:create" matches "patient:create"
     *   - Role short form: "ADMIN" matches "ADMIN" or "ROLE_ADMIN"
     *   - Role long form: "ROLE_ADMIN" matches "ROLE_ADMIN" or "ADMIN"
     *   - Scope: "SCOPE_read" matches "SCOPE_read"
     */
    private static boolean hasAuthority(Collection<String> userAuthorities, String required) {
        // Exact match
        if (userAuthorities.contains(required)) {
            return true;
        }

        // If it looks like a role (no special characters like : or _SCOPE),
        // also check with ROLE_ prefix
        if (!required.contains(":") && !required.startsWith("SCOPE_")) {
            if (required.startsWith("ROLE_")) {
                // ROLE_ADMIN → also check for ADMIN
                return userAuthorities.contains(required.substring(5));
            } else {
                // ADMIN → also check for ROLE_ADMIN
                return userAuthorities.contains("ROLE_" + required);
            }
        }

        return false;
    }

    /**
     * Get the required authorities for a specific operation from the annotation.
     */
    private static String[] getAuthoritiesForOperation(AlicePermission permission, Operation operation) {
        return switch (operation) {
            case LIST -> permission.list();
            case RETRIEVE -> permission.retrieve();
            case CREATE -> permission.create();
            case UPDATE -> permission.update();
            case PARTIAL_UPDATE -> permission.partialUpdate();
            case DELETE -> permission.delete();
        };
    }

    /**
     * Check if Spring Security is on the classpath (checked once at class load time).
     */
    private static boolean checkSpringSecurityAvailable() {
        try {
            Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Get the current user's granted authorities via reflection.
     * Uses reflection so Spring Security remains an optional dependency —
     * AliceMVC has ZERO Spring Security imports.
     *
     * @return collection of authority strings, or null if not authenticated
     */
    private static Collection<String> getCurrentUserAuthorities() {
        try {
            Class<?> contextHolderClass = Class.forName(
                    "org.springframework.security.core.context.SecurityContextHolder");
            Object context = contextHolderClass.getMethod("getContext").invoke(null);
            Object authentication = context.getClass().getMethod("getAuthentication").invoke(context);

            if (authentication == null) {
                return null;
            }

            Boolean isAuthenticated = (Boolean) authentication.getClass()
                    .getMethod("isAuthenticated").invoke(authentication);
            if (!isAuthenticated) {
                return null;
            }

            // Check for anonymous authentication
            String authClassName = authentication.getClass().getSimpleName();
            if ("AnonymousAuthenticationToken".equals(authClassName)) {
                return null;
            }

            Collection<?> authorities = (Collection<?>) authentication.getClass()
                    .getMethod("getAuthorities").invoke(authentication);

            return authorities.stream()
                    .map(auth -> {
                        try {
                            return (String) auth.getClass().getMethod("getAuthority").invoke(auth);
                        } catch (Exception e) {
                            return auth.toString();
                        }
                    })
                    .toList();

        } catch (Exception e) {
            return null;
        }
    }
}
