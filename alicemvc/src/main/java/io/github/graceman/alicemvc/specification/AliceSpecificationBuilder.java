package io.github.graceman.alicemvc.specification;

import io.github.graceman.alicemvc.annotation.SearchField;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds JPA {@link Specification}s from {@code @SearchField} annotations
 * and query parameters.
 *
 * @author Graceman
 * @since 1.0.0
 */
public class AliceSpecificationBuilder {

    /**
     * Build a specification from filter parameters and the entity's
     * {@code @SearchField} annotations.
     *
     * @param entityClass  the entity class
     * @param filterParams the filter parameters from the request
     * @param <T>          entity type
     * @return the combined specification, or null if no filters match
     */
    public static <T> Specification<T> buildFromSearchFields(
            Class<T> entityClass, Map<String, String> filterParams) {

        if (filterParams == null || filterParams.isEmpty()) {
            return null;
        }

        List<Specification<T>> specs = new ArrayList<>();

        for (Field field : getAllFields(entityClass)) {
            SearchField annotation = field.getAnnotation(SearchField.class);
            if (annotation == null) continue;

            String paramValue = filterParams.get(field.getName());
            if (paramValue == null || paramValue.isBlank()) continue;

            specs.add(buildFieldSpec(field, annotation.operator(), paramValue));
        }

        if (specs.isEmpty()) return null;

        Specification<T> combined = specs.get(0);
        for (int i = 1; i < specs.size(); i++) {
            combined = combined.and(specs.get(i));
        }
        return combined;
    }

    /**
     * Build a global text search specification across all String
     * fields annotated with {@code @SearchField}.
     *
     * @param entityClass the entity class
     * @param searchTerm  the search term
     * @param <T>         entity type
     * @return the search specification, or null
     */
    public static <T> Specification<T> buildSearchSpec(
            Class<T> entityClass, String searchTerm) {

        if (searchTerm == null || searchTerm.isBlank()) return null;

        List<Specification<T>> orSpecs = new ArrayList<>();
        String pattern = "%" + searchTerm.toLowerCase() + "%";

        for (Field field : getAllFields(entityClass)) {
            if (field.getAnnotation(SearchField.class) == null) continue;
            if (!String.class.isAssignableFrom(field.getType())) continue;

            orSpecs.add((root, query, cb) ->
                    cb.like(cb.lower(root.get(field.getName())), pattern));
        }

        if (orSpecs.isEmpty()) return null;

        Specification<T> combined = orSpecs.get(0);
        for (int i = 1; i < orSpecs.size(); i++) {
            combined = combined.or(orSpecs.get(i));
        }
        return combined;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Specification<T> buildFieldSpec(
            Field field, SearchField.Operator operator, String value) {

        return (root, query, cb) -> {
            Path<?> path = root.get(field.getName());
            Class<?> fieldType = field.getType();

            // Handle enums
            if (fieldType.isEnum()) {
                Object enumValue = Enum.valueOf((Class<Enum>) fieldType, value.toUpperCase());
                return cb.equal(path, enumValue);
            }

            // Handle dates
            if (LocalDate.class.isAssignableFrom(fieldType)) {
                LocalDate dateValue = LocalDate.parse(value);
                return buildComparison(cb, root.get(field.getName()), dateValue, operator);
            }
            if (LocalDateTime.class.isAssignableFrom(fieldType)) {
                LocalDateTime dateValue = LocalDateTime.parse(value);
                return buildComparison(cb, root.get(field.getName()), dateValue, operator);
            }

            // Handle numbers
            if (Number.class.isAssignableFrom(fieldType) || fieldType.isPrimitive()) {
                if (fieldType == Integer.class || fieldType == int.class) {
                    return buildComparison(cb, root.get(field.getName()), Integer.parseInt(value), operator);
                }
                if (fieldType == Long.class || fieldType == long.class) {
                    return buildComparison(cb, root.get(field.getName()), Long.parseLong(value), operator);
                }
                if (fieldType == Double.class || fieldType == double.class) {
                    return buildComparison(cb, root.get(field.getName()), Double.parseDouble(value), operator);
                }
            }

            // Handle booleans
            if (fieldType == Boolean.class || fieldType == boolean.class) {
                return cb.equal(path, Boolean.parseBoolean(value));
            }

            // Default: String
            if (operator == SearchField.Operator.LIKE) {
                return cb.like(cb.lower((Path<String>) path),
                        "%" + value.toLowerCase() + "%");
            }
            return cb.equal(path, value);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Predicate buildComparison(
            CriteriaBuilder cb, Path path, Comparable value, SearchField.Operator operator) {
        return switch (operator) {
            case GTE -> cb.greaterThanOrEqualTo(path, value);
            case LTE -> cb.lessThanOrEqualTo(path, value);
            case GT -> cb.greaterThan(path, value);
            case LT -> cb.lessThan(path, value);
            default -> cb.equal(path, value);
        };
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                fields.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
