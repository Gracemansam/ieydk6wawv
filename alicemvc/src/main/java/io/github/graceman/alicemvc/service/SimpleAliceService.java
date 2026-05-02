package io.github.graceman.alicemvc.service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;

/**
 * Simplified service for entity-as-DTO mode (no separate DTOs needed).
 *
 * <p>Use this when your entity IS your request and response.
 * Only two generic parameters instead of five.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * @Service
 * public class DepartmentService extends SimpleAliceService<Department, Long> {
 *     public DepartmentService(DepartmentRepository repo) {
 *         super(repo);
 *     }
 * }
 * }</pre>
 *
 * <p>All hooks and override points are still available:</p>
 * <pre>{@code
 * @Service
 * public class ProductService extends SimpleAliceService<Product, Long> {
 *
 *     public ProductService(ProductRepository repo) { super(repo); }
 *
 *     @Override
 *     protected void beforeCreate(HookContext<Product, Long> context) {
 *         Product p = context.getEntity();
 *         p.setSku("SKU-" + UUID.randomUUID().toString().substring(0, 8));
 *     }
 *
 *     @Override
 *     protected void performDelete(Product entity, Long id) {
 *         entity.setArchived(true);
 *         getRepository().save(entity); // soft delete
 *     }
 * }
 * }</pre>
 *
 * <p>When you need separate DTOs later, switch to
 * {@link AliceService}{@code <T, ID, R, C, U>} with an
 * {@link io.github.graceman.alicemvc.mapper.EntityMapper}.</p>
 *
 * @param <T>  entity type
 * @param <ID> primary key type (Long, UUID, etc.)
 * @author Graceman
 * @since 1.0.0
 */
public abstract class SimpleAliceService<T, ID extends Serializable>
        extends AliceService<T, ID, T, T, T> {

    protected SimpleAliceService(JpaRepository<T, ID> repository) {
        super(repository);
    }
}
