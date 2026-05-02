package io.github.graceman.clinic.service;

import io.github.graceman.alicemvc.service.SimpleAliceService;
import io.github.graceman.clinic.entity.Department;
import io.github.graceman.clinic.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

/**
 * DEMO: Zero-boilerplate service using SimpleAliceService.
 * No hooks, no mapper, no custom logic — just CRUD.
 */
@Service
public class DepartmentService extends SimpleAliceService<Department, Long> {
    public DepartmentService(DepartmentRepository repository) {
        super(repository);
    }
}
