package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.body_region.BodyRegion;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.patient.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CaseRepositoryTest {

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnCasesOrderedByCreatedAtWithinClinic() {
        Clinics clinicOne = persistClinic("clinic-one");
        Clinics clinicTwo = persistClinic("clinic-two");
        Patient firstPatient = persistPatient(clinicOne, "Sam", "Lee");
        Patient secondPatient = persistPatient(clinicTwo, "Alex", "Kim");
        BodyRegion shoulder = persistBodyRegion("SHOULDER", "Shoulder");
        BodyRegion knee = persistBodyRegion("KNEE", "Knee");
        Case earlyCase = persistCase(clinicOne, firstPatient, shoulder, Date.from(Instant.parse("2026-03-01T10:00:00Z")));
        Case lateCase = persistCase(clinicOne, firstPatient, knee, Date.from(Instant.parse("2026-03-02T10:00:00Z")));
        persistCase(clinicTwo, secondPatient, shoulder, Date.from(Instant.parse("2026-03-03T10:00:00Z")));
        entityManager.flush();
        entityManager.clear();

        List<Case> result = caseRepository.getCaseByPatient(firstPatient.getId(), clinicOne.getId());

        assertEquals(2, result.size());
        assertEquals(earlyCase.getId(), result.get(0).getId());
        assertEquals(lateCase.getId(), result.get(1).getId());
    }

    @Test
    void shouldReturnCaseByIdWithinClinic() {
        Clinics clinic = persistClinic("clinic-one");
        Patient patient = persistPatient(clinic, "Sam", "Lee");
        BodyRegion shoulder = persistBodyRegion("SHOULDER", "Shoulder");
        Case savedCase = persistCase(clinic, patient, shoulder, Date.from(Instant.parse("2026-03-01T10:00:00Z")));
        entityManager.flush();
        entityManager.clear();

        Optional<Case> result = caseRepository.findByIdAndClinicId(savedCase.getId(), clinic.getId());

        assertTrue(result.isPresent());
        assertEquals(savedCase.getId(), result.get().getId());
    }

    private Clinics persistClinic(String slug) {
        Clinics clinic = new Clinics();
        clinic.setName(slug);
        clinic.setSlug(slug);
        clinic.setIsDemo(false);
        clinic.setCreatedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
        entityManager.persist(clinic);
        return clinic;
    }

    private Patient persistPatient(Clinics clinic, String firstName, String lastName) {
        Patient patient = new Patient();
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setDisplayName(lastName + ", " + firstName.charAt(0));
        patient.setClinic(clinic);
        entityManager.persist(patient);
        return patient;
    }

    private BodyRegion persistBodyRegion(String code, String displayName) {
        BodyRegion bodyRegion = new BodyRegion();
        bodyRegion.setCode(code);
        bodyRegion.setDisplayName(displayName);
        bodyRegion.setIsActive(true);
        entityManager.persist(bodyRegion);
        return bodyRegion;
    }

    private Case persistCase(Clinics clinic, Patient patient, BodyRegion bodyRegion, Date createdAt) {
        Case caseEntity = new Case();
        caseEntity.setPatient(patient);
        caseEntity.setBodyRegion(bodyRegion);
        caseEntity.setCreatedAt(createdAt);
        caseEntity.setClinic(clinic);
        entityManager.persist(caseEntity);
        return caseEntity;
    }
}
