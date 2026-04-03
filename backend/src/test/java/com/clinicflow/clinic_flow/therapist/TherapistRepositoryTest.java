package com.clinicflow.clinic_flow.therapist;

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
    void shouldReturnTherapistsMatchingName_whenSearchUsesNameTerm() {
        Therapist exact = persistTherapist("Sam Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        persistTherapist("Jordan Lee", Therapist.TherapistType.OCCUPATIONAL_THERAPIST);
        entityManager.flush();
        entityManager.clear();

        Page<Therapist> result = therapistRepository.search("sam", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(exact.getId(), result.getContent().get(0).getId());
    }

    @Test
    void shouldReturnTherapistsMatchingEnumText_whenSearchUsesTypeTerm() {
        Therapist match = persistTherapist("Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        persistTherapist("Jordan", Therapist.TherapistType.OCCUPATIONAL_THERAPIST);
        entityManager.flush();
        entityManager.clear();

        Page<Therapist> result = therapistRepository.search("physical", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(match.getId(), result.getContent().get(0).getId());
    }

    @Test
    void shouldRespectPagination_whenSearchMatchesMultipleTherapists() {
        persistTherapist("Sam Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        persistTherapist("Sam Jordan", Therapist.TherapistType.PHYSICAL_THERAPY_ASSISTANT);
        persistTherapist("Alex Lee", Therapist.TherapistType.OCCUPATIONAL_THERAPIST);
        entityManager.flush();
        entityManager.clear();

        Page<Therapist> result = therapistRepository.search("sam", PageRequest.of(0, 1));

        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void shouldReturnEmptyPage_whenSearchDoesNotMatchAnyTherapist() {
        persistTherapist("Sam Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        entityManager.flush();
        entityManager.clear();

        Page<Therapist> result = therapistRepository.search("zoe", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    private Therapist persistTherapist(String name, Therapist.TherapistType type) {
        Therapist therapist = new Therapist();
        therapist.setTherapistName(name);
        therapist.setType(type);
        entityManager.persist(therapist);
        return therapist;
    }
}
