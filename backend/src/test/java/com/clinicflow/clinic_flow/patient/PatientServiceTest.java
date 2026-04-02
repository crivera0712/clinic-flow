package com.clinicflow.clinic_flow.patient;

import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
import com.clinicflow.clinic_flow.patient.dtos.PatientPatchDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientRequestDto;
import com.clinicflow.clinic_flow.patient.dtos.PatientResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientService patientService;

    @Test
    void shouldReturnPatientResponses_whenGetPatientsFindsPatients() {
        // Arrange
        Patient firstPatient = patient(1L, "Sam", "Lee");
        Patient secondPatient = patient(2L, "Alex", "Kim");
        PatientResponseDto firstResponse = response(1L, "Sam", "Lee");
        PatientResponseDto secondResponse = response(2L, "Alex", "Kim");
        Pageable pageable = PageRequest.of(0, 10);

        when(patientRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(firstPatient, secondPatient)));
        when(patientMapper.toPatientResponseDto(firstPatient)).thenReturn(firstResponse);
        when(patientMapper.toPatientResponseDto(secondPatient)).thenReturn(secondResponse);

        // Act
        Page<PatientResponseDto> result = patientService.getPatients(pageable);

        // Assert
        assertEquals(List.of(firstResponse, secondResponse), result.getContent());
        verify(patientRepository).findAll(pageable);
        verify(patientMapper).toPatientResponseDto(firstPatient);
        verify(patientMapper).toPatientResponseDto(secondPatient);
    }

    @Test
    void shouldReturnEmptyList_whenGetPatientsFindsNoPatients() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(patientRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        // Act
        Page<PatientResponseDto> result = patientService.getPatients(pageable);

        // Assert
        assertTrue(result.isEmpty());
        verify(patientRepository).findAll(pageable);
        verify(patientMapper, never()).toPatientResponseDto(any(Patient.class));
    }

    @Test
    void shouldReturnPatientResponse_whenGetPatientFindsPatient() {
        // Arrange
        Patient patient = patient(5L, "Sam", "Lee");
        PatientResponseDto response = response(5L, "Sam", "Lee");

        when(patientRepository.getPatientById(5L)).thenReturn(patient);
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        // Act
        PatientResponseDto result = patientService.getPatient(5L);

        // Assert
        assertSame(response, result);
        verify(patientRepository).getPatientById(5L);
        verify(patientMapper).toPatientResponseDto(patient);
    }

    @Test
    void shouldThrowPatientNotFoundException_whenGetPatientDoesNotFindPatient() {
        // Arrange
        when(patientRepository.getPatientById(5L)).thenReturn(null);

        // Act
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.getPatient(5L));

        // Assert
        assertEquals("Patient with id 5 does not exist", exception.getMessage());
        verify(patientRepository).getPatientById(5L);
        verify(patientMapper, never()).toPatientResponseDto(any(Patient.class));
    }

    @Test
    void shouldReturnPatients_whenSearchPatientReceivesOneToken() {
        // Arrange
        Patient patient = patient(1L, "Sam", "Lee");
        PatientResponseDto response = response(1L, "Sam", "Lee");

        when(patientRepository.searchPatientByOneToken("sam")).thenReturn(List.of(patient));
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        // Act
        List<PatientResponseDto> result = patientService.searchPatient("sam");

        // Assert
        assertEquals(List.of(response), result);
        verify(patientRepository).searchPatientByOneToken("sam");
        verify(patientRepository, never()).searchPatientByTwoTokens(any(), any());
        verify(patientMapper).toPatientResponseDto(patient);
    }

    @Test
    void shouldReturnPatients_whenSearchPatientReceivesTwoTokens() {
        // Arrange
        Patient patient = patient(1L, "Sam", "Lee");
        PatientResponseDto response = response(1L, "Sam", "Lee");

        when(patientRepository.searchPatientByTwoTokens("sam", "lee")).thenReturn(List.of(patient));
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        // Act
        List<PatientResponseDto> result = patientService.searchPatient("sam lee");

        // Assert
        assertEquals(List.of(response), result);
        verify(patientRepository).searchPatientByTwoTokens("sam", "lee");
        verify(patientRepository, never()).searchPatientByOneToken("sam");
        verify(patientMapper).toPatientResponseDto(patient);
    }

    @Test
    void shouldTrimWhitespace_whenSearchPatientReceivesExtraSpaces() {
        // Arrange
        Patient patient = patient(1L, "Sam", "Lee");
        PatientResponseDto response = response(1L, "Sam", "Lee");

        when(patientRepository.searchPatientByTwoTokens("sam", "lee")).thenReturn(List.of(patient));
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        // Act
        List<PatientResponseDto> result = patientService.searchPatient("   sam    lee   ");

        // Assert
        assertEquals(List.of(response), result);
        verify(patientRepository).searchPatientByTwoTokens("sam", "lee");
    }

    @Test
    void shouldFallbackToFirstToken_whenSearchPatientReceivesMoreThanTwoTokens() {
        // Arrange
        Patient patient = patient(1L, "Sam", "Lee");
        PatientResponseDto response = response(1L, "Sam", "Lee");

        when(patientRepository.searchPatientByOneToken("sam")).thenReturn(List.of(patient));
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        // Act
        List<PatientResponseDto> result = patientService.searchPatient("sam lee extra");

        // Assert
        assertEquals(List.of(response), result);
        verify(patientRepository).searchPatientByOneToken("sam");
        verify(patientRepository, never()).searchPatientByTwoTokens(any(), any());
    }

    @Test
    void shouldThrowIllegalArgumentException_whenSearchPatientReceivesBlankInput() {
        // Arrange

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> patientService.searchPatient("   "));

        // Assert
        assertEquals("search query must not be blank", exception.getMessage());
        verify(patientRepository, never()).searchPatientByOneToken(any());
        verify(patientRepository, never()).searchPatientByTwoTokens(any(), any());
    }

    @Test
    void shouldCreatePatient_whenCreatePatientReceivesValidRequest() {
        // Arrange
        PatientRequestDto request = new PatientRequestDto();
        request.setFirstName("Sam");
        request.setLastName("Lee");
        Patient mappedPatient = patient(null, "Sam", "Lee");
        Patient savedPatient = patient(8L, "Sam", "Lee");
        PatientResponseDto response = response(8L, "Sam", "Lee");

        when(patientMapper.toPatient(request)).thenReturn(mappedPatient);
        when(patientRepository.save(mappedPatient)).thenReturn(savedPatient);
        when(patientMapper.toPatientResponseDto(savedPatient)).thenReturn(response);

        // Act
        PatientResponseDto result = patientService.createPatient(request);

        // Assert
        assertSame(response, result);
        verify(patientMapper).toPatient(request);
        verify(patientRepository).save(mappedPatient);
        verify(patientMapper).toPatientResponseDto(savedPatient);
    }

    @Test
    void shouldUpdatePatient_whenUpdatePatientReceivesFullPatch() {
        // Arrange
        Patient patient = patient(9L, "Sam", "Lee");
        PatientPatchDto patch = patch("Samuel", "Leeds");
        PatientResponseDto response = response(9L, "Samuel", "Leeds");

        when(patientRepository.findById(9L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        // Act
        PatientResponseDto result = patientService.updatePatient(9L, patch);

        // Assert
        assertSame(response, result);
        assertEquals("Samuel", patient.getFirstName());
        assertEquals("Leeds", patient.getLastName());
        verify(patientRepository).findById(9L);
        verify(patientRepository).save(patient);
        verify(patientMapper).toPatientResponseDto(patient);
    }

    @Test
    void shouldUpdateOnlyFirstName_whenUpdatePatientReceivesFirstNameOnlyPatch() {
        // Arrange
        Patient patient = patient(9L, "Sam", "Lee");
        PatientPatchDto patch = patch("Samuel", null);
        PatientResponseDto response = response(9L, "Samuel", "Lee");

        when(patientRepository.findById(9L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        // Act
        PatientResponseDto result = patientService.updatePatient(9L, patch);

        // Assert
        assertSame(response, result);
        assertEquals("Samuel", patient.getFirstName());
        assertEquals("Lee", patient.getLastName());
        verify(patientRepository).save(patient);
    }

    @Test
    void shouldUpdateOnlyLastName_whenUpdatePatientReceivesLastNameOnlyPatch() {
        // Arrange
        Patient patient = patient(9L, "Sam", "Lee");
        PatientPatchDto patch = patch(null, "Leeds");
        PatientResponseDto response = response(9L, "Sam", "Leeds");

        when(patientRepository.findById(9L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        // Act
        PatientResponseDto result = patientService.updatePatient(9L, patch);

        // Assert
        assertSame(response, result);
        assertEquals("Sam", patient.getFirstName());
        assertEquals("Leeds", patient.getLastName());
        verify(patientRepository).save(patient);
    }

    @Test
    void shouldKeepExistingValues_whenUpdatePatientReceivesEmptyPatch() {
        // Arrange
        Patient patient = patient(9L, "Sam", "Lee");
        PatientPatchDto patch = patch(null, null);
        PatientResponseDto response = response(9L, "Sam", "Lee");

        when(patientRepository.findById(9L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toPatientResponseDto(patient)).thenReturn(response);

        // Act
        PatientResponseDto result = patientService.updatePatient(9L, patch);

        // Assert
        assertSame(response, result);
        assertEquals("Sam", patient.getFirstName());
        assertEquals("Lee", patient.getLastName());
        verify(patientRepository).save(patient);
    }

    @Test
    void shouldThrowPatientNotFoundException_whenUpdatePatientDoesNotFindPatient() {
        // Arrange
        PatientPatchDto patch = patch("Samuel", "Leeds");
        when(patientRepository.findById(9L)).thenReturn(Optional.empty());

        // Act
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> patientService.updatePatient(9L, patch));

        // Assert
        assertEquals("Patient with id 9 does not exist", exception.getMessage());
        verify(patientRepository).findById(9L);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    private Patient patient(Long id, String firstName, String lastName) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setDisplayName(lastName + ", " + firstName.charAt(0));
        return patient;
    }

    private PatientResponseDto response(Long id, String firstName, String lastName) {
        return new PatientResponseDto(id, firstName, lastName, lastName + ", " + firstName.charAt(0));
    }

    private PatientPatchDto patch(String firstName, String lastName) {
        PatientPatchDto patch = new PatientPatchDto();
        setField(patch, "firstName", firstName);
        setField(patch, "lastName", lastName);
        return patch;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
