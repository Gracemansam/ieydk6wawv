package io.github.graceman.clinic.controller;

import io.github.graceman.alicemvc.annotation.DisableOperation;
import io.github.graceman.alicemvc.annotation.DisableOperation.Operation;
import io.github.graceman.alicemvc.controller.SimpleAliceController;
import io.github.graceman.alicemvc.dto.ApiResponse;
import io.github.graceman.alicemvc.exception.ResourceNotFoundException;
import io.github.graceman.clinic.entity.Patient;
import io.github.graceman.clinic.repository.PatientRepository;
import io.github.graceman.clinic.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * DEMO: Full-featured controller with custom endpoints, disabled
 * operations, and per-controller response override.
 *
 * Auto-generated endpoints (DELETE disabled):
 *   GET    /api/v1/patients                 — List
 *   GET    /api/v1/patients/{id}            — Get by ID
 *   POST   /api/v1/patients                 — Create
 *   PUT    /api/v1/patients/{id}            — Update
 *   PATCH  /api/v1/patients/{id}            — Partial update
 *   DELETE /api/v1/patients/{id}            — BLOCKED (405)
 *
 * Custom endpoints:
 *   GET    /api/v1/patients/by-mrn/{mrn}    — Find by MRN
 */
@RestController
@RequestMapping("/api/v1/patients")
@DisableOperation({Operation.DELETE})
public class PatientController extends SimpleAliceController<Patient, Long> {

    private final PatientRepository patientRepository;

    public PatientController(PatientService service, PatientRepository patientRepository) {
        super(service);
        this.patientRepository = patientRepository;
    }

    // ═══════════════════════════════════════════════════════
    //  Custom Endpoint — alongside auto-CRUD
    // ═══════════════════════════════════════════════════════

    @GetMapping("/by-mrn/{mrn}")
    public ResponseEntity<?> findByMrn(@PathVariable("mrn") String mrn) {
        Patient patient = patientRepository.findByMrn(mrn)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "MRN=" + mrn));
        return respondOk(patient);
    }

    // ═══════════════════════════════════════════════════════
    //  Per-Controller Response Override
    //  Only affects this controller — others use ResponseFactory
    // ═══════════════════════════════════════════════════════

    @Override
    protected ResponseEntity<?> respondCreated(Patient data) {
        return ResponseEntity.status(201).body(
                ApiResponse.success(data, "Patient registered with MRN: " + data.getMrn()));
    }
}
