# AliceMVC Framework

*A lightweight, Django ViewSet-inspired CRUD framework for Spring Boot.*
*Named in loving memory of my Grandma Alice.*

---

## What is AliceMVC?

AliceMVC removes the repetitive boilerplate from Spring Boot REST APIs. Instead of writing the same controller, service, and exception handling code for every entity, you extend two base classes and get a complete, production-ready API — with pagination, filtering, sorting, search, validation, lifecycle hooks, and consistent error handling — all out of the box.

**Before AliceMVC** (what you normally write for ONE entity):

- Controller with 6 endpoint methods (~80 lines)
- Service with CRUD logic (~100 lines)
- Exception handling (~40 lines)
- Response wrapping (~30 lines)
- Pagination logic (~20 lines)

**With AliceMVC** (what you actually write):

```java
@Service
public class ProductService extends SimpleAliceService<Product, Long> {
    public ProductService(ProductRepository repo) { super(repo); }
}

@RestController
@RequestMapping("/api/v1/products")
public class ProductController extends SimpleAliceController<Product, Long> {
    public ProductController(ProductService service) { super(service); }
}
```

That's it. Two tiny classes. You get 6 REST endpoints, pagination, filtering, sorting, global search, validation, error handling, and lifecycle hooks — all for free.

---

## Table of Contents

1. [Quick Start](#1-quick-start)
2. [What You Get for Free](#2-what-you-get-for-free)
3. [Your First API — Step by Step](#3-your-first-api--step-by-step)
4. [Filtering and Search](#4-filtering-and-search)
5. [Lifecycle Hooks — Running Code Before/After CRUD](#5-lifecycle-hooks--running-code-beforeafter-crud)
6. [Three Ways to Hook Into CRUD](#6-three-ways-to-hook-into-crud)
7. [Context Attributes — Passing Data Between Hooks](#7-context-attributes--passing-data-between-hooks)
8. [Hook Cancellation — Stopping an Operation](#8-hook-cancellation--stopping-an-operation)
9. [Soft Delete](#9-soft-delete)
10. [Custom Endpoints — Alongside Auto-CRUD](#10-custom-endpoints--alongside-auto-crud)
11. [Disabling Operations](#11-disabling-operations)
12. [Custom Filtering with Specifications](#12-custom-filtering-with-specifications)
13. [Pagination Defaults — Per Controller](#13-pagination-defaults--per-controller)
14. [Response Customization](#14-response-customization)
15. [Business Exceptions](#15-business-exceptions)
16. [Exception Handler](#16-exception-handler)
17. [Full DTO Mode — Separate Request/Response Objects](#17-full-dto-mode--separate-requestresponse-objects)
18. [SimpleAliceController vs AliceController — When to Use Which](#18-simplealicecontroller-vs-alicecontroller--when-to-use-which)
19. [API Response Format](#19-api-response-format)
20. [Architecture Overview](#20-architecture-overview)
21. [Configuration Reference](#21-configuration-reference)
22. [Publishing to Maven Central](#22-publishing-to-maven-central)

---

## 1. Quick Start

### Step 1 — Add the dependency

```xml
<dependency>
    <groupId>io.github.graceman</groupId>
    <artifactId>alicemvc-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Step 2 — Enable AliceMVC

Add `@EnableAliceMVC` to your main application class:

```java
@SpringBootApplication
@EnableAliceMVC
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}
```

### Step 3 — Create your entity

```java
@Entity
public class Product implements Identifiable<Long> {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private Double price;

    // Getters and setters...

    @Override
    public Long getId() { return id; }
}
```

> **Note:** Your entity must implement `Identifiable<ID>` — it just needs a `getId()` method.

### Step 4 — Create a repository

```java
public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
}
```

> **Important:** Always extend both `JpaRepository` AND `JpaSpecificationExecutor`. The second one enables filtering and search.

### Step 5 — Create a service

```java
@Service
public class ProductService extends SimpleAliceService<Product, Long> {
    public ProductService(ProductRepository repo) {
        super(repo);
    }
}
```

### Step 6 — Create a controller

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController extends SimpleAliceController<Product, Long> {
    public ProductController(ProductService service) {
        super(service);
    }
}
```

### Step 7 — Run and test

```bash
mvn spring-boot:run
```

Your API is live:

```
GET    /api/v1/products           → List all (paginated)
GET    /api/v1/products/1         → Get by ID
POST   /api/v1/products           → Create
PUT    /api/v1/products/1         → Full update
PATCH  /api/v1/products/1         → Partial update
DELETE /api/v1/products/1         → Delete
```

---

## 2. What You Get for Free

Every controller that extends `SimpleAliceController` or `AliceController` automatically gets:

- **6 REST endpoints** — LIST, GET, POST, PUT, PATCH, DELETE
- **Pagination** — `?page=0&size=20` with configurable defaults and max limits
- **Sorting** — `?sort=name&direction=asc`
- **Filtering** — `?status=ACTIVE&category=ELECTRONICS` on `@SearchField` fields
- **Global search** — `?search=john` searches across all String `@SearchField` fields
- **Bean validation** — `@NotBlank`, `@Email`, `@Size` etc. automatically enforced
- **Consistent JSON responses** — every response wrapped in `ApiResponse` with `success`, `message`, `data`, `timestamp`
- **Error handling** — validation errors, not-found, business rule violations all return clean JSON
- **Lifecycle hooks** — run code before/after any CRUD operation
- **Spring events** — `AliceCrudEvent` published after every operation

---

## 3. Your First API — Step by Step

Let's build a simple Task API from scratch.

### 3.1 — The Entity

```java
@Entity
@Table(name = "tasks")
public class Task implements Identifiable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private Status status = Status.TODO;

    public enum Status { TODO, IN_PROGRESS, DONE }

    // --- Getters and Setters ---
    @Override
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
```

### 3.2 — The Repository

```java
public interface TaskRepository
        extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
}
```

### 3.3 — The Service

```java
@Service
public class TaskService extends SimpleAliceService<Task, Long> {
    public TaskService(TaskRepository repo) {
        super(repo);
    }
}
```

### 3.4 — The Controller

```java
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController extends SimpleAliceController<Task, Long> {
    public TaskController(TaskService service) {
        super(service);
    }
}
```

### 3.5 — Test it

Create a task:

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Buy groceries", "description": "Milk, eggs, bread"}'
```

Response:

```json
{
    "success": true,
    "message": "Created successfully",
    "data": {
        "id": 1,
        "title": "Buy groceries",
        "description": "Milk, eggs, bread",
        "status": "TODO"
    },
    "timestamp": "2026-05-02T10:30:00"
}
```

List all tasks:

```bash
curl http://localhost:8080/api/v1/tasks
```

Response:

```json
{
    "success": true,
    "message": "Retrieved successfully",
    "data": {
        "content": [
            {"id": 1, "title": "Buy groceries", "description": "Milk, eggs, bread", "status": "TODO"}
        ],
        "page": 0,
        "size": 20,
        "totalElements": 1,
        "totalPages": 1,
        "first": true,
        "last": true
    },
    "timestamp": "2026-05-02T10:30:00"
}
```

---

## 4. Filtering and Search

### 4.1 — Mark fields as filterable with `@SearchField`

```java
@Entity
public class Task implements Identifiable<Long> {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @SearchField(operator = SearchField.Operator.LIKE)
    private String title;

    @SearchField
    @Enumerated(EnumType.STRING)
    private Status status;

    @SearchField(operator = SearchField.Operator.GTE)
    private LocalDate dueDate;

    // ...
}
```

### 4.2 — Use query parameters to filter

```
GET /api/v1/tasks?status=TODO                  → tasks with status = TODO
GET /api/v1/tasks?title=groceries              → tasks with "groceries" in title (LIKE)
GET /api/v1/tasks?dueDate=2026-06-01           → tasks due on or after June 1 (GTE)
GET /api/v1/tasks?status=TODO&title=buy        → combine filters (AND)
GET /api/v1/tasks?search=milk                  → search "milk" across all String @SearchField fields
```

### 4.3 — Available operators

| Operator | Meaning | Example |
|----------|---------|---------|
| `EQUALS` (default) | Exact match | `?status=ACTIVE` |
| `LIKE` | Contains (case-insensitive) | `?name=john` matches "Johnson" |
| `GTE` | Greater than or equal | `?price=100` → price >= 100 |
| `LTE` | Less than or equal | `?price=500` → price <= 500 |
| `GT` | Greater than | `?age=18` → age > 18 |
| `LT` | Less than | `?age=65` → age < 65 |

---

## 5. Lifecycle Hooks — Running Code Before/After CRUD

Hooks let you add custom logic at any point in the CRUD lifecycle — validation, auto-generation, notifications, audit logging, etc.

### 5.1 — Override hooks in your service

```java
@Service
public class TaskService extends SimpleAliceService<Task, Long> {

    public TaskService(TaskRepository repo) { super(repo); }

    @Override
    protected void beforeCreate(HookContext<Task, Long> context) {
        Task task = context.getEntity();

        // Auto-set creation timestamp
        task.setCreatedAt(LocalDateTime.now());

        // Generate a task number
        task.setTaskNumber("TASK-" + System.currentTimeMillis());
    }

    @Override
    protected void afterCreate(HookContext<Task, Long> context) {
        Task task = context.getEntity();
        System.out.println("Task created: " + task.getTaskNumber());
        // Send notification, update dashboard, etc.
    }

    @Override
    protected void beforeUpdate(HookContext<Task, Long> context) {
        Task task = context.getEntity();
        task.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    protected void beforeDelete(HookContext<Task, Long> context) {
        Task task = context.getEntity();
        if (task.getStatus() == Task.Status.IN_PROGRESS) {
            // Cancel the delete — see Section 8
            context.cancel("Cannot delete a task that is in progress");
        }
    }
}
```

### 5.2 — Available hooks

| Hook | When it runs |
|------|-------------|
| `beforeCreate(context)` | Before a new entity is saved |
| `afterCreate(context)` | After a new entity is saved |
| `beforeUpdate(context)` | Before an existing entity is updated (PUT) |
| `afterUpdate(context)` | After an existing entity is updated (PUT) |
| `beforePartialUpdate(context)` | Before a partial update (PATCH) |
| `afterPartialUpdate(context)` | After a partial update (PATCH) |
| `beforeDelete(context)` | Before an entity is deleted |
| `afterDelete(context)` | After an entity is deleted |

---

## 6. Three Ways to Hook Into CRUD

AliceMVC gives you three hook mechanisms. Use whichever fits your use case, or combine all three on the same entity.

### Way 1 — Inline hooks (override in your service)

Best for core business logic that's tightly coupled to the entity.

```java
@Service
public class OrderService extends SimpleAliceService<Order, Long> {
    public OrderService(OrderRepository repo) { super(repo); }

    @Override
    protected void beforeCreate(HookContext<Order, Long> context) {
        context.getEntity().setOrderNumber(generateOrderNumber());
    }
}
```

### Way 2 — Standalone hook beans (implement CrudHook)

Best for cross-cutting concerns like audit logging, notifications, or validation that should be decoupled from the service.

```java
@Component
public class OrderAuditHook implements CrudHook<Order, Long> {

    @Override
    public Class<Order> getEntityClass() { return Order.class; }

    @Override
    public void afterCreate(HookContext<Order, Long> context) {
        auditService.log("Order created: " + context.getEntity().getOrderNumber());
    }

    @Override
    public void afterDelete(HookContext<Order, Long> context) {
        auditService.log("Order deleted: " + context.getEntityId());
    }
}
```

The framework auto-discovers all `CrudHook` beans and runs them automatically.

### Way 3 — Spring events (use @EventListener)

Best for fully decoupled reactions — sending emails, syncing to external systems, async processing.

```java
@Component
public class CrudEventLogger {

    @EventListener
    public void onAnyCrudEvent(AliceCrudEvent<?> event) {
        System.out.println(event.getAction() + " on " +
            event.getEntityClass().getSimpleName() +
            " (ID: " + event.getEntityId() + ")");
    }

    @EventListener(condition = "#event.action.name() == 'CREATE'")
    public void onCreated(AliceCrudEvent<?> event) {
        // Only fires for CREATE operations
    }

    @EventListener(condition = "#event.action.name() == 'DELETE'")
    public void onDeleted(AliceCrudEvent<?> event) {
        // Only fires for DELETE operations
    }
}
```

### Execution order

When all three are used on the same entity, they run in this order:

1. **Inline hooks** (service overrides) — first
2. **Standalone hook beans** (CrudHook implementations) — second
3. **Spring events** (AliceCrudEvent published) — last

---

## 7. Context Attributes — Passing Data Between Hooks

The `HookContext` object carries a map of custom attributes that lets you pass data from a `before*` hook to the corresponding `after*` hook.

```java
@Override
protected void beforeCreate(HookContext<Patient, Long> context) {
    Patient patient = context.getEntity();

    // Generate MRN
    String mrn = "MRN-" + UUID.randomUUID().toString().substring(0, 8);
    patient.setMrn(mrn);

    // Store generation time — will be available in afterCreate
    context.setAttribute("mrnGeneratedAt", LocalDateTime.now());
}

@Override
protected void afterCreate(HookContext<Patient, Long> context) {
    Patient patient = context.getEntity();
    LocalDateTime mrnTime = context.getAttribute("mrnGeneratedAt");

    System.out.println("Patient " + patient.getMrn() +
        " registered at " + mrnTime);
}
```

You can store any object as an attribute. Use `context.setAttribute("key", value)` to store, and `context.getAttribute("key")` to retrieve.

---

## 8. Hook Cancellation — Stopping an Operation

Call `context.cancel("reason")` in any `before*` hook to prevent the operation from completing. The framework returns a 422 error with your reason.

```java
@Override
protected void beforeDelete(HookContext<Patient, Long> context) {
    Patient patient = context.getEntity();

    if (patient.getStatus() == Patient.Status.ACTIVE) {
        context.cancel("Cannot delete an active patient. Discharge first.");
        // The delete will NOT happen.
        // Client receives: 422 {"success": false, "message": "Cannot delete..."}
    }
}
```

```java
@Override
protected void beforeCreate(HookContext<Appointment, Long> context) {
    Appointment apt = context.getEntity();

    boolean conflict = appointmentRepo.existsByDoctorIdAndTimeSlot(
        apt.getDoctorId(), apt.getTimeSlot());

    if (conflict) {
        context.cancel("This time slot is already booked for this doctor.");
    }
}
```

---

## 9. Soft Delete

By default, `DELETE` removes the record from the database. Override `performDelete()` to implement soft delete instead:

```java
@Service
public class PatientService extends SimpleAliceService<Patient, Long> {

    public PatientService(PatientRepository repo) { super(repo); }

    @Override
    protected void performDelete(Patient entity, Long id) {
        // Instead of deleting, set a flag
        entity.setDeleted(true);
        entity.setStatus(Patient.Status.INACTIVE);
        getRepository().save(entity);
    }
}
```

Combine with a custom specification to automatically exclude soft-deleted records from listings (see Section 12).

---

## 10. Custom Endpoints — Alongside Auto-CRUD

Add any `@GetMapping`, `@PostMapping`, etc. to your controller alongside the auto-generated CRUD endpoints. They don't interfere with each other.

```java
@RestController
@RequestMapping("/api/v1/patients")
public class PatientController extends SimpleAliceController<Patient, Long> {

    private final PatientRepository patientRepository;

    public PatientController(PatientService service, PatientRepository repo) {
        super(service);
        this.patientRepository = repo;
    }

    // This custom endpoint coexists with the auto-generated CRUD endpoints
    @GetMapping("/by-mrn/{mrn}")
    public ResponseEntity<?> findByMrn(@PathVariable("mrn") String mrn) {
        Patient patient = patientRepository.findByMrn(mrn)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "MRN=" + mrn));
        return respondOk(patient);  // Uses the same ResponseFactory as CRUD endpoints
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long total = patientRepository.count();
        return respondOk(Map.of("totalPatients", total));
    }
}
```

Notice you can use `respondOk()`, `respondCreated()`, etc. in your custom endpoints too — they use the same `ResponseFactory` as the auto-generated CRUD endpoints, so all responses stay consistent.

---

## 11. Disabling Operations

Use `@DisableOperation` on your controller to block specific CRUD operations. Blocked operations return HTTP 405 (Method Not Allowed).

```java
@RestController
@RequestMapping("/api/v1/appointments")
@DisableOperation({Operation.DELETE})
public class AppointmentController extends SimpleAliceController<Appointment, Long> {
    public AppointmentController(AppointmentService service) { super(service); }
}
// DELETE /api/v1/appointments/1 → 405 "DELETE operation is not allowed on Appointment"
```

You can disable multiple operations:

```java
@DisableOperation({Operation.DELETE, Operation.UPDATE, Operation.PARTIAL_UPDATE})
// Only LIST, RETRIEVE, and CREATE work — read-only + create
```

Available operations: `LIST`, `RETRIEVE`, `CREATE`, `UPDATE`, `PARTIAL_UPDATE`, `DELETE`

---

## 12. Custom Filtering with Specifications

For filters beyond simple field matching (date ranges, computed conditions, always-exclude-soft-deleted), override `getCustomSpecification()` in your service:

```java
@Override
protected Specification<Patient> getCustomSpecification(
        Map<String, String> filters, HttpServletRequest request) {

    // Always exclude soft-deleted records
    Specification<Patient> spec = (root, query, cb) ->
            cb.equal(root.get("deleted"), false);

    // Custom filter: age range
    String minAge = filters.get("minAge");
    if (minAge != null) {
        LocalDate cutoff = LocalDate.now().minusYears(Long.parseLong(minAge));
        spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("dateOfBirth"), cutoff));
    }

    return spec;
}
```

Your custom specification is automatically ANDed with `@SearchField` filters. So `GET /api/v1/patients?status=ACTIVE&minAge=18` combines the `@SearchField` filter on status with your custom age filter AND the soft-delete exclusion.

---

## 13. Pagination Defaults — Per Controller

Override these methods in your controller to change pagination behavior for that specific endpoint:

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController extends SimpleAliceController<Product, Long> {

    public ProductController(ProductService service) { super(service); }

    @Override
    protected int getDefaultPageSize() { return 50; }       // Default: 20

    @Override
    protected int getMaxPageSize() { return 200; }           // Default: 100

    @Override
    protected String getDefaultSortField() { return "name"; } // Default: "id"

    @Override
    protected String getDefaultSortDirection() { return "asc"; } // Default: "desc"
}
```

If the developer doesn't override these, sensible defaults apply (page size 20, max 100, sorted by id descending).

---

## 14. Response Customization

### 14.1 — Change response format globally

By default, all responses are wrapped in `ApiResponse`:

```json
{
    "success": true,
    "message": "Retrieved successfully",
    "data": { ... },
    "timestamp": "2026-05-02T10:30:00"
}
```

To change this for your entire application, create your own `ResponseFactory`:

```java
@Component
public class FlatResponseFactory implements ResponseFactory {

    @Override
    public <T> ResponseEntity<?> ok(T data, String message) {
        return ResponseEntity.ok(data);  // No envelope — raw data
    }

    @Override
    public <T> ResponseEntity<?> created(T data, String message) {
        return ResponseEntity.status(201).body(data);
    }

    @Override
    public ResponseEntity<?> deleted(String message) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public <T> ResponseEntity<?> page(Page<T> page, String message) {
        return ResponseEntity.ok(page);  // Spring's native Page
    }

    @Override
    public ResponseEntity<?> error(String message, Object errors, int status) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
```

Just define it as a `@Component`. The framework's `DefaultResponseFactory` backs off automatically — no properties or configuration needed.

### 14.2 — Change response format for one controller

Override any `respond*` method in your controller:

```java
@Override
protected ResponseEntity<?> respondCreated(Patient data) {
    return ResponseEntity.status(201).body(Map.of(
        "patient", data,
        "message", "Patient registered with MRN: " + data.getMrn()
    ));
}

@Override
protected ResponseEntity<?> respondDeleted() {
    return ResponseEntity.ok(Map.of("deleted", true));
}
```

This only affects that one controller. All other controllers continue using the `ResponseFactory`.

---

## 15. Business Exceptions

Use `BusinessException` when a business rule is violated. It returns HTTP 422 (Unprocessable Entity) — semantically correct for "the request syntax is valid, but the business logic rejects it."

```java
@Override
protected void beforeCreate(HookContext<Order, Long> context) {
    Order order = context.getEntity();

    if (order.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessException("Order total must be positive");
    }

    if (inventoryService.isOutOfStock(order.getProductId())) {
        throw new BusinessException("Product is out of stock");
    }
}
```

Response:

```json
{
    "success": false,
    "message": "Product is out of stock",
    "timestamp": "2026-05-02T10:30:00"
}
```

Status code: **422 Unprocessable Entity**

When to use which exception:

| Exception | HTTP Status | When to use |
|-----------|------------|-------------|
| `BusinessException` | 422 | Business rule violated (duplicate email, out of stock, etc.) |
| `ResourceNotFoundException` | 404 | Entity not found by ID or lookup |
| `context.cancel("reason")` | 422 | Stop a CRUD operation from inside a hook |
| `IllegalArgumentException` | 400 | Bad input format |

---

## 16. Exception Handler

AliceMVC includes a global exception handler that catches all common exceptions and returns clean JSON — no stack traces leak to the client.

### 16.1 — Disable it entirely

If you have your own `@RestControllerAdvice`, disable AliceMVC's handler:

```properties
# application.properties
alicemvc.exception-handler.enabled=false
```

### 16.2 — Replace by extending

```java
@RestControllerAdvice
public class MyExceptionHandler extends AliceExceptionHandler {

    public MyExceptionHandler(ResponseFactory responseFactory) {
        super(responseFactory);
    }

    @Override
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        // Your custom 404 handling
        return ResponseEntity.status(404).body(Map.of(
            "error", "Not found",
            "detail", ex.getMessage()
        ));
    }
}
```

The framework's handler backs off automatically when yours is detected.

### 16.3 — Change error format globally

The exception handler uses the same `ResponseFactory` as the controllers. So if you define a custom `ResponseFactory` (see Section 14.1), error responses automatically match your chosen format.

---

## 17. Full DTO Mode — Separate Request/Response Objects

For production APIs where you want different shapes for create, update, and response — use the full `AliceController` and `AliceService` with an `EntityMapper`.

### 17.1 — Define your DTOs

```java
// What the client sends to create
public class CreatePatientRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email private String email;
    // getters and setters
}

// What the client sends to update
public class UpdatePatientRequest {
    private String firstName;
    private String lastName;
    private String email;
    // getters and setters
}

// What the client receives
public class PatientResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mrn;
    private String status;
    // getters and setters
}
```

### 17.2 — Create a mapper

```java
@Component
public class PatientMapper implements EntityMapper<Patient, PatientResponse, CreatePatientRequest, UpdatePatientRequest> {

    @Override
    public PatientResponse toResponse(Patient entity) {
        PatientResponse r = new PatientResponse();
        r.setId(entity.getId());
        r.setFirstName(entity.getFirstName());
        r.setLastName(entity.getLastName());
        r.setEmail(entity.getEmail());
        r.setMrn(entity.getMrn());
        r.setStatus(entity.getStatus().name());
        return r;
    }

    @Override
    public Patient fromCreateRequest(CreatePatientRequest req) {
        Patient p = new Patient();
        p.setFirstName(req.getFirstName());
        p.setLastName(req.getLastName());
        p.setEmail(req.getEmail());
        return p;
    }

    @Override
    public void applyUpdate(UpdatePatientRequest req, Patient entity) {
        if (req.getFirstName() != null) entity.setFirstName(req.getFirstName());
        if (req.getLastName() != null) entity.setLastName(req.getLastName());
        if (req.getEmail() != null) entity.setEmail(req.getEmail());
    }
}
```

### 17.3 — Use full AliceService and AliceController

```java
@Service
public class PatientService
        extends AliceService<Patient, Long, PatientResponse, CreatePatientRequest, UpdatePatientRequest> {

    public PatientService(PatientRepository repo, PatientMapper mapper) {
        super(repo, mapper);
    }
}
```

```java
@RestController
@RequestMapping("/api/v1/patients")
public class PatientController
        extends AliceController<Patient, Long, PatientResponse, CreatePatientRequest, UpdatePatientRequest> {

    public PatientController(PatientService service) {
        super(service);
    }
}
```

---

## 18. SimpleAliceController vs AliceController — When to Use Which

| | SimpleAliceController | AliceController |
|---|---|---|
| **Generic params** | 2 (`<Entity, ID>`) | 5 (`<Entity, ID, Response, Create, Update>`) |
| **DTOs** | Entity is the DTO | Separate request/response DTOs |
| **Mapper** | Not needed | Required (`EntityMapper`) |
| **Best for** | Prototyping, internal APIs, simple CRUD | Production APIs, public APIs, complex domains |
| **Hooks** | Full access | Full access |
| **Override points** | All available | All available |
| **Migration** | Can switch to full mode anytime | N/A |

**Start with `SimpleAliceController`.** Switch to `AliceController` when you need to hide entity fields from clients or have different shapes for create vs. update.

---

## 19. API Response Format

### Success response (single item)

```json
{
    "success": true,
    "message": "Retrieved successfully",
    "data": {
        "id": 1,
        "name": "Cardiology",
        "description": "Heart and cardiovascular care"
    },
    "timestamp": "2026-05-02T10:30:00"
}
```

### Success response (paginated list)

```json
{
    "success": true,
    "message": "Retrieved successfully",
    "data": {
        "content": [ ... ],
        "page": 0,
        "size": 20,
        "totalElements": 45,
        "totalPages": 3,
        "first": true,
        "last": false
    },
    "timestamp": "2026-05-02T10:30:00"
}
```

### Validation error (400)

```json
{
    "success": false,
    "message": "Validation failed",
    "errors": ["firstName: must not be blank", "email: Invalid email format"],
    "timestamp": "2026-05-02T10:30:00"
}
```

### Business error (422)

```json
{
    "success": false,
    "message": "A patient with email 'john@example.com' already exists",
    "timestamp": "2026-05-02T10:30:00"
}
```

### Not found (404)

```json
{
    "success": false,
    "message": "Patient not found with id: 999",
    "timestamp": "2026-05-02T10:30:00"
}
```

---

## 20. Architecture Overview

```
Your Application
├── Entity implements Identifiable<ID>
├── Repository extends JpaRepository + JpaSpecificationExecutor
├── Service extends SimpleAliceService<T, ID>
│   ├── beforeCreate / afterCreate
│   ├── beforeUpdate / afterUpdate
│   ├── beforePartialUpdate / afterPartialUpdate
│   ├── beforeDelete / afterDelete
│   ├── performDelete (override for soft delete)
│   └── getCustomSpecification (override for custom filters)
├── Controller extends SimpleAliceController<T, ID>
│   ├── getDefaultPageSize / getMaxPageSize (override per controller)
│   ├── getDefaultSortField / getDefaultSortDirection
│   ├── respondOk / respondCreated / respondDeleted (override per controller)
│   ├── extractFilterParams / isPaginationParam (override to customize)
│   └── + your custom @GetMapping/@PostMapping endpoints
└── Hook beans implement CrudHook<T, ID> (optional)

AliceMVC Framework (what you get for free)
├── AliceController / SimpleAliceController  → REST endpoints
├── AliceService / SimpleAliceService        → CRUD logic + hooks
├── ResponseFactory / DefaultResponseFactory → Response format (replaceable)
├── HookRegistry                             → Auto-discovers CrudHook beans
├── HookContext                              → Data bridge between hooks
├── AliceSpecificationBuilder                → Builds JPA Specs from @SearchField
├── AliceCrudEvent                           → Spring event after CRUD ops
├── AliceExceptionHandler                    → Global error handling (togglable)
├── BusinessException                        → 422 for business rule violations
├── @EnableAliceMVC                          → Activates auto-configuration
├── @DisableOperation                        → Blocks specific CRUD endpoints
├── @SearchField                             → Marks fields as filterable
└── EntityMapper                             → DTO mapping contract
```

---

## 21. Configuration Reference

### application.properties / application.yml

```properties
# Disable the built-in exception handler (default: true)
alicemvc.exception-handler.enabled=false
```

### Replaceable beans

| Bean | How to replace |
|------|---------------|
| `ResponseFactory` | Define your own `@Component` implementing `ResponseFactory` |
| `AliceExceptionHandler` | Extend it, or disable via property and write your own |

### Per-controller overrides

| Method | Default | What it controls |
|--------|---------|-----------------|
| `getDefaultPageSize()` | 20 | Default page size |
| `getMaxPageSize()` | 100 | Maximum page size allowed |
| `getDefaultSortField()` | "id" | Default sort field |
| `getDefaultSortDirection()` | "desc" | Default sort direction |
| `respondList(page)` | Uses ResponseFactory | Response for list endpoints |
| `respondOk(data)` | Uses ResponseFactory | Response for single-item endpoints |
| `respondCreated(data)` | Uses ResponseFactory | Response for create endpoints |
| `respondUpdated(data)` | Uses ResponseFactory | Response for update endpoints |
| `respondDeleted()` | Uses ResponseFactory | Response for delete endpoints |
| `extractFilterParams(params)` | Excludes pagination params | Which query params are filters |
| `isPaginationParam(name)` | page, size, sort, direction, search | Which params are pagination |
| `formatFieldError(field, msg)` | "field: message" | Validation error format |

### Per-service overrides

| Method | What it controls |
|--------|-----------------|
| `beforeCreate / afterCreate` | Hook into create |
| `beforeUpdate / afterUpdate` | Hook into update |
| `beforePartialUpdate / afterPartialUpdate` | Hook into partial update |
| `beforeDelete / afterDelete` | Hook into delete |
| `performDelete(entity, id)` | Hard delete vs. soft delete |
| `getCustomSpecification(filters, request)` | Custom JPA filters |

---

## 22. Publishing to Maven Central

### Step 1 — Create a Sonatype OSSRH account

Go to https://central.sonatype.com and sign up.

### Step 2 — Verify your groupId

Since your groupId is `io.github.graceman`, verify ownership of the GitHub account `graceman` at https://central.sonatype.com/publishing.

### Step 3 — Generate a GPG key

```bash
gpg --gen-key
gpg --list-keys
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### Step 4 — Configure Maven settings

Add to `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>central</id>
            <username>YOUR_SONATYPE_USERNAME</username>
            <password>YOUR_SONATYPE_TOKEN</password>
        </server>
    </servers>
</settings>
```

### Step 5 — Add publishing plugins to parent POM

Add the `maven-gpg-plugin`, `nexus-staging-maven-plugin`, and distribution management sections to the parent POM. See the [Sonatype OSSRH guide](https://central.sonatype.org/publish/publish-maven/) for the full configuration.

### Step 6 — Publish

```bash
mvn clean deploy -P release
```

Only the `alicemvc` and `alicemvc-spring-boot-starter` modules should be published. The `clinic-api` is a sample app, not a library.

---

## Requirements

- Java 17+
- Spring Boot 3.x
- Spring Data JPA

## License

MIT License — free for personal and commercial use.

---

*Built with love. In memory of Grandma Alice.*
# ieydk6wawv
