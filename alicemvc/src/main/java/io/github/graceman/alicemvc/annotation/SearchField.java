package io.github.graceman.alicemvc.annotation;

import java.lang.annotation.*;

/**
 * Marks an entity field as filterable via query parameters.
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * @Entity
 * public class Patient {
 *     @SearchField(operator = Operator.LIKE)
 *     private String firstName;
 *
 *     @SearchField
 *     @Enumerated(EnumType.STRING)
 *     private Status status;
 *
 *     @SearchField(operator = Operator.GTE)
 *     private LocalDate dateOfBirth;
 * }
 * }</pre>
 *
 * <p>Requests: {@code GET /patients?firstName=john&status=ACTIVE&dateOfBirth=1990-01-01}</p>
 *
 * @author Graceman
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SearchField {

    /**
     * The comparison operator to use when filtering.
     *
     * @return the operator (default: EQUALS)
     */
    Operator operator() default Operator.EQUALS;

    enum Operator {
        EQUALS, LIKE, GTE, LTE, GT, LT
    }
}
