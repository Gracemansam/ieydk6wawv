package io.github.graceman.clinic.controller;

import io.github.graceman.alicemvc.controller.SimpleAliceController;
import io.github.graceman.clinic.entity.Department;
import io.github.graceman.clinic.service.DepartmentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DEMO: Zero-boilerplate CRUD controller.
 *
 * Endpoints (all auto-generated):
 *   GET    /api/v1/departments           — List (paginated, filterable)
 *   GET    /api/v1/departments/{id}      — Get by ID
 *   POST   /api/v1/departments           — Create
 *   PUT    /api/v1/departments/{id}      — Full update
 *   PATCH  /api/v1/departments/{id}      — Partial update
 *   DELETE /api/v1/departments/{id}      — Delete
 *
 * Filtering: ?name=cardio&description=heart
 */
@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController extends SimpleAliceController<Department, Long> {

    public DepartmentController(DepartmentService service) {
        super(service);
    }

    // Optional: override pagination defaults for this controller
    @Override
    protected int getDefaultPageSize() { return 50; }

    @Override
    protected String getDefaultSortField() { return "name"; }

    @Override
    protected String getDefaultSortDirection() { return "asc"; }
}
