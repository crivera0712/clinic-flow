package com.clinicflow.clinic_flow.patient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.exception.DemoClinicReadOnlyException;
import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
import com.clinicflow.clinic_flow.patient.dtos.PatientPatchDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientRequestDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientResponseDto;
import com.clinicflow.clinic_flow.users.CurrentUserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ClinicContextService clinicContextService;

    @InjectMocks
    private PatientService patientService;

    @Test
    void shouldReturnPatientResponsesWithinCurrentClinic() {
        Patient firstPatient = patient(1L, "Sam", "Lee");
        Patient secondPatient = patient(2L, "Alex", "Kim");
        PatientResponseDto firstResponse = response(1L, "Sam", "Lee");
        PatientResponseDto secondResponse = response(2L, "Alex", "Kim");
        Pageable pageable = PageRequest.of(0, 10);

        when(currentUserService.getCurrentClinicId()).thenReturn(8L);
        when(patientRepository.findAllByClinicId(8L, pageable))
                .thenReturn(new PageImpl<>(List.of(firstPatient, secondPatient)));
        when(patientMapper.toPatientResponseDto(firstPatient)).thenReturn(firstResponse);
        when(patientMapper.toPatientResponseDto(secondPatient)).thenReturn(secondResponse);

        Page<PatientResponseDto> result = patientService.getPatients(pageable);

        assertEquals(List.of(firstResponse, secondResponse), result.getContent());
        verify(patientRepository).findAllByClinicId(8L, pageable);
    }

    @Test
    void shouldReturnPatientWithinCurrentClinic() {
        Patient patient = patient(5L, "Sam", "Lee");
        PatientResponseDto response = response(5L, "Sam", "Lee");

        when(currentUserService.getCurrentClinicId()).thenReturn(8L);
        when(patientRepository.findByIdAndClinicId(5L, 8L)).thenReturn(Optional.of(patient));
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        PatientResponseDto result = patientService.getPatient(5L);

        assertSame(response, result);
        verify(patientRepository).findByIdAndClinicId(5L, 8L);
    }

    @Test
    void shouldSearchPatientsWithinCurrentClinic() {
        Patient patient = patient(1L, "Sam", "Lee");
        PatientResponseDto response = response(1L, "Sam", "Lee");

        when(currentUserService.getCurrentClinicId()).thenReturn(8L);
        when(patientRepository.searchPatientByTwoTokens("sam", "lee", 8L)).thenReturn(List.of(patient));
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        List<PatientResponseDto> result = patientService.searchPatient("sam lee");

        assertEquals(List.of(response), result);
        verify(patientRepository).searchPatientByTwoTokens("sam", "lee", 8L);
    }

    @Test
    void shouldCreatePatientWithinCurrentClinic() {
        PatientRequestDto request = new PatientRequestDto();
        request.setFirstName("Sam");
        request.setLastName("Lee");
        Patient mapped = patient(null, "Sam", "Lee");
        Patient saved = patient(8L, "Sam", "Lee");
        PatientResponseDto response = response(8L, "Sam", "Lee");
        Clinics clinic = clinic(8L);

        when(clinicContextService.requireWritableClinic()).thenReturn(clinic);
        when(patientMapper.toPatient(request)).thenReturn(mapped);
        when(patientRepository.save(mapped)).thenReturn(saved);
        when(patientMapper.toPatientResponseDto(saved)).thenReturn(response);

        PatientResponseDto result = patientService.createPatient(request);

        assertSame(response, result);
        assertSame(clinic, mapped.getClinic());
    }

    @Test
    void shouldThrowPatientNotFoundWhenClinicScopedLookupMisses() {
        when(currentUserService.getCurrentClinicId()).thenReturn(8L);
        when(patientRepository.findByIdAndClinicId(5L, 8L)).thenReturn(Optional.empty());

        PatientNotFoundException exception =
                assertThrows(PatientNotFoundException.class, () -> patientService.getPatient(5L));

        assertEquals("Patient with id 5 does not exist", exception.getMessage());
    }

    @Test
    void shouldThrowWhenDemoClinicCreatesPatient() {
        PatientRequestDto request = new PatientRequestDto();
        request.setFirstName("Sam");
        request.setLastName("Lee");

        when(clinicContextService.requireWritableClinic()).thenThrow(new DemoClinicReadOnlyException());

        assertThrows(DemoClinicReadOnlyException.class, () -> patientService.createPatient(request));

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void shouldThrowWhenDemoClinicUpdatesPatient() {
        org.mockito.Mockito.doThrow(new DemoClinicReadOnlyException())
                .when(clinicContextService)
                .assertWritableClinic();

        assertThrows(DemoClinicReadOnlyException.class, () -> patientService.updatePatient(5L, new PatientPatchDto()));

        verify(patientRepository, never()).findByIdAndClinicId(any(), any());
    }

    @Test
    void shouldThrowWhenDemoClinicDeletesPatient() {
        org.mockito.Mockito.doThrow(new DemoClinicReadOnlyException())
                .when(clinicContextService)
                .assertWritableClinic();

        assertThrows(DemoClinicReadOnlyException.class, () -> patientService.deletePatient(5L));

        verify(patientRepository, never()).delete(any(Patient.class));
    }

    @Test
    void shouldThrowWhenUpdateTargetsPatientFromAnotherClinic() {
        PatientPatchDto patch = new PatientPatchDto();

        when(currentUserService.getCurrentClinicId()).thenReturn(8L);
        when(patientRepository.findByIdAndClinicId(5L, 8L)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> patientService.updatePatient(5L, patch));

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void shouldThrowWhenDeleteTargetsPatientFromAnotherClinic() {
        when(currentUserService.getCurrentClinicId()).thenReturn(8L);
        when(patientRepository.findByIdAndClinicId(5L, 8L)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> patientService.deletePatient(5L));

        verify(patientRepository, never()).delete(any(Patient.class));
    }

    private Patient patient(Long id, String firstName, String lastName) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        return patient;
    }

    private PatientResponseDto response(Long id, String firstName, String lastName) {
        return new PatientResponseDto(id, firstName, lastName);
    }

    private Clinics clinic(Long id) {
        Clinics clinic = new Clinics();
        clinic.setId(id);
        clinic.setSlug("clinic-" + id);
        return clinic;
    }
}
