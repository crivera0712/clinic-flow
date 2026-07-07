package com.clinicflow.clinic_flow.therapist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.clinicflow.clinic_flow.clinics.Clinics;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class TherapistRepositoryTest {

    @Autowired
    private TherapistRepository therapistRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByClinicId_returnsOnlyThatClinicsTherapists() {
        Clinics clinicOne = persistClinic("clinic-one");
        Clinics clinicTwo = persistClinic("clinic-two");
        persistTherapist(clinicOne, "Sam Taylor");
        persistTherapist(clinicOne, "Jordan Lee");
        persistTherapist(clinicTwo, "Sam Other");
        entityManager.flush();
        entityManager.clear();

        List<Therapist> result = therapistRepository.findAllByClinicId(clinicOne.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getClinic().getId().equals(clinicOne.getId())));
    }

    @Test
    void findByIdAndClinicId_isClinicScoped() {
        Clinics clinic = persistClinic("clinic-one");
        Therapist therapist = persistTherapist(clinic, "Sam Taylor");
        entityManager.flush();
        entityManager.clear();

        assertTrue(therapistRepository
                .findByIdAndClinicId(therapist.getId(), clinic.getId())
                .isPresent());
        Optional<Therapist> wrongClinic = therapistRepository.findByIdAndClinicId(therapist.getId(), 999L);
        assertFalse(wrongClinic.isPresent());
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

    private Therapist persistTherapist(Clinics clinic, String name) {
        Therapist therapist = new Therapist();
        therapist.setTherapistName(name);
        therapist.setClinic(clinic);
        entityManager.persist(therapist);
        return therapist;
    }
}
