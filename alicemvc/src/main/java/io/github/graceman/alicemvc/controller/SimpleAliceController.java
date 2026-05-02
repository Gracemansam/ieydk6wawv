package io.github.graceman.alicemvc.controller;

import io.github.graceman.alicemvc.dto.ResponseFactory;
import io.github.graceman.alicemvc.service.AliceService;

import java.io.Serializable;

/**
 * Simplified controller for entity-as-DTO mode (no separate DTOs needed).
 *
 * <p>Use this when your entity IS your request and response.
 * Only two generic parameters instead of five.</p>
 *
 * <p><b>Minimal example — full CRUD in 4 lines:</b></p>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/v1/departments")
 * public class DepartmentController extends SimpleAliceController<Department, Long> {
 *     public DepartmentController(DepartmentService service) {
 *         super(service);
 *     }
 * }
 * }</pre>
 *
 * <p>All override points from {@link AliceController} are still available:</p>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/v1/products")
 * public class ProductController extends SimpleAliceController<Product, Long> {
 *
 *     public ProductController(ProductService service) { super(service); }
 *
 *     // Override pagination defaults
 *     @Override
 *     protected int getDefaultPageSize() { return 50; }
 *
 *     // Override response for just this controller
 *     @Override
 *     protected ResponseEntity<?> respondCreated(Product data) {
 *         return ResponseEntity.status(201).body(Map.of(
 *             "product", data,
 *             "message", "Product created with SKU: " + data.getSku()
 *         ));
 *     }
 *
 *     // Add custom endpoints alongside auto-CRUD
 *     @GetMapping("/by-sku/{sku}")
 *     public ResponseEntity<?> findBySku(@PathVariable String sku) { ... }
 * }
 * }</pre>
 *
 * <p>When you need separate DTOs later, switch to
 * {@link AliceController}{@code <T, ID, R, C, U>}.</p>
 *
 * @param <T>  entity type
 * @param <ID> primary key type (Long, UUID, etc.)
 * @author Graceman
 * @since 1.0.0
 */
public abstract class SimpleAliceController<T, ID extends Serializable>
        extends AliceController<T, ID, T, T, T> {

    /**
     * Creates a controller with auto-injected ResponseFactory.
     *
     * @param service the service providing CRUD operations
     */
    protected SimpleAliceController(AliceService<T, ID, T, T, T> service) {
        super(service);
    }

    /**
     * Creates a controller with explicit ResponseFactory.
     *
     * @param service         the service providing CRUD operations
     * @param responseFactory the response factory
     */
    protected SimpleAliceController(AliceService<T, ID, T, T, T> service, ResponseFactory responseFactory) {
        super(service, responseFactory);
    }
}
