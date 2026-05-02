package io.github.graceman.alicemvc.controller;

import io.github.graceman.alicemvc.annotation.DisableOperation;
import io.github.graceman.alicemvc.annotation.DisableOperation.Operation;
import io.github.graceman.alicemvc.dto.ResponseFactory;
import io.github.graceman.alicemvc.exception.OperationNotAllowedException;
import io.github.graceman.alicemvc.service.AliceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.*;

/**
 * Generic REST controller providing all CRUD endpoints out of the box.
 * Extend this class, point it to your service, and you get a full API.
 *
 * <p><b>Minimal setup (zero-boilerplate):</b></p>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/v1/doctors")
 * public class DoctorController extends AliceController<Doctor, Long, Doctor, Doctor, Doctor> {
 *     public DoctorController(DoctorService service, ResponseFactory responseFactory) {
 *         super(service, responseFactory);
 *     }
 * }
 * }</pre>
 *
 * <p><b>Backwards-compatible constructor (ResponseFactory auto-injected later):</b></p>
 * <pre>{@code
 * public DoctorController(DoctorService service) {
 *     super(service);
 * }
 * }</pre>
 *
 * <p><b>Endpoints generated:</b></p>
 * <ul>
 *   <li>{@code GET    /}      — List all (paginated, filtered, searchable)</li>
 *   <li>{@code GET    /{id}}  — Find by ID</li>
 *   <li>{@code POST   /}      — Create</li>
 *   <li>{@code PUT    /{id}}  — Full update</li>
 *   <li>{@code PATCH  /{id}}  — Partial update</li>
 *   <li>{@code DELETE /{id}}  — Delete</li>
 * </ul>
 *
 * @param <T>  entity type
 * @param <ID> primary key type
 * @param <R>  response DTO type
 * @param <C>  create request DTO type
 * @param <U>  update request DTO type
 * @author Graceman
 * @since 1.0.0
 */
public abstract class AliceController<T, ID extends Serializable, R, C, U> {

    private final AliceService<T, ID, R, C, U> service;
    private final Set<Operation> disabledOperations;
    private ResponseFactory responseFactory;

    /**
     * Creates a new AliceController with explicit ResponseFactory.
     *
     * @param service         the AliceService providing CRUD operations
     * @param responseFactory the response factory for building HTTP responses
     */
    protected AliceController(AliceService<T, ID, R, C, U> service, ResponseFactory responseFactory) {
        this.service = service;
        this.responseFactory = responseFactory;
        this.disabledOperations = resolveDisabledOperations();
    }

    /**
     * Creates a new AliceController. ResponseFactory will be injected
     * automatically by {@link io.github.graceman.alicemvc.config.AliceMvcAutoConfiguration}.
     *
     * @param service the AliceService providing CRUD operations
     */
    protected AliceController(AliceService<T, ID, R, C, U> service) {
        this.service = service;
        this.disabledOperations = resolveDisabledOperations();
    }

    /**
     * Setter for ResponseFactory — called by the framework's auto-configuration
     * if not provided via constructor.
     *
     * @param responseFactory the response factory
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setResponseFactory(ResponseFactory responseFactory) {
        if (this.responseFactory == null) {
            this.responseFactory = responseFactory;
        }
    }

    // ========================
    // CRUD Endpoints
    // ========================

    /**
     * GET / — List all entities with pagination, sorting, filtering, and search.
     *
     * @param page    the page number (default 0)
     * @param size    the page size (uses getDefaultPageSize if not specified)
     * @param sort    the sort field (uses getDefaultSortField if not specified)
     * @param search  optional global text search across SearchField String fields
     * @param request the HTTP servlet request
     * @return paginated response
     */
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "direction", required = false) String direction,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam Map<String, String> allParams,
            HttpServletRequest request) {

        guardOperation(Operation.LIST);

        int pageSize = Math.min(
                size != null ? size : getDefaultPageSize(),
                getMaxPageSize()
        );
        String sortField = sort != null ? sort : getDefaultSortField();
        String sortDir = direction != null ? direction : getDefaultSortDirection();

        Sort sorting = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(page, pageSize, sorting);

        // Extract filter params (exclude pagination params)
        Map<String, String> filterParams = extractFilterParams(allParams);

        Page<R> result = service.findAll(pageable, filterParams, search, request);

        return respondList(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") ID id) {
        guardOperation(Operation.RETRIEVE);
        R response = service.findById(id);
        return respondOk(response);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody C createRequest, HttpServletRequest request) {
        guardOperation(Operation.CREATE);
        R response = service.create(createRequest, request);
        return respondCreated(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") ID id, @Valid @RequestBody U updateRequest,
                                    HttpServletRequest request) {
        guardOperation(Operation.UPDATE);
        R response = service.update(id, updateRequest, request);
        return respondUpdated(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdate(@PathVariable("id") ID id, @RequestBody U updateRequest,
                                           HttpServletRequest request) {
        guardOperation(Operation.PARTIAL_UPDATE);
        R response = service.partialUpdate(id, updateRequest, request);
        return respondUpdated(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") ID id, HttpServletRequest request) {
        guardOperation(Operation.DELETE);
        service.delete(id, request);
        return respondDeleted();
    }

    // ════════════════════════════════════════════════════════════════
    //  Response Methods — override any of these per controller
    // ════════════════════════════════════════════════════════════════

    /**
     * Build the response for a paginated list.
     * Override to customize list responses for this controller only.
     *
     * @param page the page result
     * @return the HTTP response
     */
    protected ResponseEntity<?> respondList(Page<R> page) {
        return getResponseFactory().page(page, "Retrieved successfully");
    }

    /**
     * Build a 200 OK response.
     * Override to customize single-item responses for this controller only.
     *
     * @param data the response payload
     * @return the HTTP response
     */
    protected ResponseEntity<?> respondOk(R data) {
        return getResponseFactory().ok(data, "Retrieved successfully");
    }

    /**
     * Build a 201 Created response.
     * Override to customize create responses for this controller only.
     *
     * @param data the created resource
     * @return the HTTP response
     */
    protected ResponseEntity<?> respondCreated(R data) {
        return getResponseFactory().created(data, "Created successfully");
    }

    /**
     * Build a 200 OK response for updates.
     * Override to customize update responses for this controller only.
     *
     * @param data the updated resource
     * @return the HTTP response
     */
    protected ResponseEntity<?> respondUpdated(R data) {
        return getResponseFactory().ok(data, "Updated successfully");
    }

    /**
     * Build a response for delete operations.
     * Override to customize delete responses for this controller only.
     *
     * @return the HTTP response
     */
    protected ResponseEntity<?> respondDeleted() {
        return getResponseFactory().deleted("Deleted successfully");
    }

    // ════════════════════════════════════════════════════════════════
    //  Pagination Defaults — override per controller
    // ════════════════════════════════════════════════════════════════

    /**
     * Default page size when {@code ?size} is not specified.
     * Override to change for this controller.
     *
     * @return default page size (default: 20)
     */
    protected int getDefaultPageSize() { return 20; }

    /**
     * Maximum allowed page size. Prevents clients from requesting
     * absurdly large pages.
     *
     * @return max page size (default: 100)
     */
    protected int getMaxPageSize() { return 100; }

    /**
     * Default sort field when {@code ?sort} is not specified.
     *
     * @return default sort field (default: "id")
     */
    protected String getDefaultSortField() { return "id"; }

    /**
     * Default sort direction when {@code ?direction} is not specified.
     *
     * @return "asc" or "desc" (default: "desc")
     */
    protected String getDefaultSortDirection() { return "desc"; }

    // ════════════════════════════════════════════════════════════════
    //  Filter Param Extraction — override to customize
    // ════════════════════════════════════════════════════════════════

    /**
     * Extract filter parameters from the request, excluding pagination params.
     * Override to customize which params are treated as filters.
     *
     * @param allParams all query parameters from the request
     * @return a map of filter field names to values
     */
    protected Map<String, String> extractFilterParams(Map<String, String> allParams) {
        Map<String, String> filterParams = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (!isPaginationParam(entry.getKey())) {
                filterParams.put(entry.getKey(), entry.getValue());
            }
        }
        return filterParams;
    }

    /**
     * Check if a query parameter is a pagination/sorting param (not a filter).
     * Override to add custom non-filter params.
     *
     * @param paramName the parameter name
     * @return true if this is a pagination param
     */
    protected boolean isPaginationParam(String paramName) {
        return Set.of("page", "size", "sort", "direction", "search").contains(paramName);
    }

    // ════════════════════════════════════════════════════════════════
    //  Error Response Formatting — override to customize
    // ════════════════════════════════════════════════════════════════

    /**
     * Format a field validation error. Override to change the format
     * of validation error messages for this controller.
     *
     * @param fieldName    the field that failed validation
     * @param errorMessage the validation error message
     * @return the formatted error string
     */
    protected String formatFieldError(String fieldName, String errorMessage) {
        return fieldName + ": " + errorMessage;
    }

    // ════════════════════════════════════════════════════════════════
    //  Internal
    // ════════════════════════════════════════════════════════════════

    protected AliceService<T, ID, R, C, U> getService() {
        return service;
    }

    protected ResponseFactory getResponseFactory() {
        if (responseFactory == null) {
            // Fallback: if no ResponseFactory was injected, use a new default
            responseFactory = new io.github.graceman.alicemvc.dto.DefaultResponseFactory();
        }
        return responseFactory;
    }

    private void guardOperation(Operation operation) {
        if (disabledOperations.contains(operation)) {
            throw new OperationNotAllowedException(
                    operation.name(),
                    service.getEntityName()
            );
        }
    }

    private Set<Operation> resolveDisabledOperations() {
        DisableOperation annotation = getClass().getAnnotation(DisableOperation.class);
        if (annotation != null) {
            return EnumSet.copyOf(Arrays.asList(annotation.value()));
        }
        return EnumSet.noneOf(Operation.class);
    }
}
