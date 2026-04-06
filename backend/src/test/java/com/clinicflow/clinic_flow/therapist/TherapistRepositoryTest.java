package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.clinics.Clinics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TherapistRepositoryTest {

    @Autowired
    private TherapistRepository therapistRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnTherapistsMatchingNameWithinClinic() {
        Clinics clinicOne = persistClinic("clinic-one");
        Clinics clinicTwo = persistClinic("clinic-two");
        Therapist exact = persistTherapist(clinicOne, "Sam Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        persistTherapist(clinicTwo, "Sam Other", Therapist.TherapistType.PHYSICAL_THERAPIST);
        persistTherapist(clinicOne, "Jordan Lee", Therapist.TherapistType.OCCUPATIONAL_THERAPIST);
        entityManager.flush();
        entityManager.clear();

        Page<Therapist> result = therapistRepository.search("sam", PageRequest.of(0, 10), clinicOne.getId());

        assertEquals(1, result.getTotalElements());
        assertEquals(exact.getId(), result.getContent().get(0).getId());
    }

    @Test
    void shouldReturnEmptyPageWhenClinicDoesNotMatch() {
        Clinics clinic = persistClinic("clinic-one");
        persistTherapist(clinic, "Sam Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        entityManager.flush();
        entityManager.clear();

        Page<Therapist> result = therapistRepository.search("sam", PageRequest.of(0, 10), 999L);

        assertTrue(result.isEmpty());
    }

    private Clinics persistClinic(String slug) {
        Clinics clinic = new Clinics();
        clinic.setName(slug);
        clinic.setSlug(slug);
        clinic.setIsDemo(false);
        clinic.setCreatedAt(java.time.LocalDateTime.of(2026, 3, 1, 10, 0));
        entityManager.persist(clinic);
        return clinic;
    }

    private Therapist persistTherapist(Clinics clinic, String name, Therapist.TherapistType type) {
        Therapist therapist = new Therapist();
        therapist.setTherapistName(name);
        therapist.setType(type);
        therapist.setClinic(clinic);
        entityManager.persist(therapist);
        return therapist;
    }
}
