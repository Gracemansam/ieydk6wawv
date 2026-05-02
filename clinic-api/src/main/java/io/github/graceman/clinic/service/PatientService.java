package io.github.graceman.clinic.service;

import io.github.graceman.alicemvc.exception.BusinessException;
import io.github.graceman.alicemvc.hook.HookContext;
import io.github.graceman.alicemvc.service.SimpleAliceService;
import io.github.graceman.clinic.entity.Patient;
import io.github.graceman.clinic.repository.PatientRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DEMO: Full-featured service with inline hooks, soft delete,
 * custom specifications, BusinessException, and context attributes.
 */
@Service
public class PatientService extends SimpleAliceService<Patient, Long> {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository repository) {
        super(repository);
        this.patientRepository = repository;
    }

    // ═══════════════════════════════════════════════════════
    //  Inline Hooks
    // ═══════════════════════════════════════════════════════

    @Override
    protected void beforeCreate(HookContext<Patient, Long> context) {
        Patient patient = context.getEntity();

        // Business rule: duplicate email check — throws BusinessException (422)
        if (patient.getEmail() != null && patientRepository.existsByEmail(patient.getEmail())) {
            throw new BusinessException("A patient with email '" + patient.getEmail() + "' already exists");
        }

        // Auto-generate Medical Record Number
        String mrn = "MRN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        patient.setMrn(mrn);

        // Pass data to afterCreate via context attributes
        context.setAttribute("mrnGeneratedAt", LocalDateTime.now());

        log.info("Generated MRN: {} for patient: {} {}", mrn, patient.getFirstName(), patient.getLastName());
    }

    @Override
    protected void afterCreate(HookContext<Patient, Long> context) {
        Patient patient = context.getEntity();
        LocalDateTime mrnTime = context.getAttribute("mrnGeneratedAt");

        log.info("Patient created — MRN: {}, generated at: {}, ID: {}",
                patient.getMrn(), mrnTime, patient.getId());
        // In real app: send welcome email, notify billing, etc.
    }

    @Override
    protected void beforeDelete(HookContext<Patient, Long> context) {
        Patient patient = context.getEntity();

        // Business rule: cannot delete active patients
        if (patient.getStatus() == Patient.Status.ACTIVE) {
            context.cancel("Cannot delete an active patient. Discharge first.");
        }
    }

    // ═══════════════════════════════════════════════════════
    //  Soft Delete Override
    // ═══════════════════════════════════════════════════════

    @Override
    protected void performDelete(Patient entity, Long id) {
        // Soft delete — set flag instead of removing from DB
        entity.setDeleted(true);
        entity.setStatus(Patient.Status.INACTIVE);
        patientRepository.save(entity);
        log.info("Soft-deleted patient ID: {}", id);
    }

    // ═══════════════════════════════════════════════════════
    //  Custom Specification — always exclude soft-deleted
    // ═══════════════════════════════════════════════════════

    @Override
    protected Specification<Patient> getCustomSpecification(
            Map<String, String> filters, HttpServletRequest request) {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }
}
