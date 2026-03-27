package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.body_region.BodyRegion;
import com.clinicflow.clinic_flow.body_region.BodyRegionRepository;
import com.clinicflow.clinic_flow.cases.dtos.CasePatchDto;
import com.clinicflow.clinic_flow.cases.dtos.CaseRequestDto;
import com.clinicflow.clinic_flow.cases.dtos.CaseResponseDto;
import com.clinicflow.clinic_flow.exception.BodyRegionNotFoundException;
import com.clinicflow.clinic_flow.exception.CaseNotFoundException;
import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
import com.clinicflow.clinic_flow.patient.Patient;
import com.clinicflow.clinic_flow.patient.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private BodyRegionRepository bodyRegionRepository;

    @InjectMocks
    private CaseService caseService;

    @Test
    void shouldReturnCaseResponses_whenGetCasesFindsCases() {
        // Arrange
        Case firstCase = ptCase(1L, 10L, 20L);
        Case secondCase = ptCase(2L, 11L, 21L);
        CaseResponseDto firstResponse = response(1L, 10L, 20L);
        CaseResponseDto secondResponse = response(2L, 11L, 21L);

        when(caseRepository.findAll()).thenReturn(List.of(firstCase, secondCase));
        when(caseMapper.toCaseResponseDto(firstCase)).thenReturn(firstResponse);
        when(caseMapper.toCaseResponseDto(secondCase)).thenReturn(secondResponse);

        // Act
        List<CaseResponseDto> result = caseService.getCases();

        // Assert
        assertEquals(List.of(firstResponse, secondResponse), result);
        verify(caseRepository).findAll();
        verify(caseMapper).toCaseResponseDto(firstCase);
        verify(caseMapper).toCaseResponseDto(secondCase);
    }

    @Test
    void shouldReturnEmptyList_whenGetCasesFindsNoCases() {
        // Arrange
        when(caseRepository.findAll()).thenReturn(List.of());

        // Act
        List<CaseResponseDto> result = caseService.getCases();

        // Assert
        assertTrue(result.isEmpty());
        verify(caseRepository).findAll();
        verify(caseMapper, never()).toCaseResponseDto(any(Case.class));
    }

    @Test
    void shouldReturnCaseResponse_whenGetCaseFindsCase() {
        // Arrange
        Case caseEntity = ptCase(5L, 10L, 20L);
        CaseResponseDto response = response(5L, 10L, 20L);

        when(caseRepository.findById(5L)).thenReturn(Optional.of(caseEntity));
        when(caseMapper.toCaseResponseDto(caseEntity)).thenReturn(response);

        // Act
        CaseResponseDto result = caseService.getCase(5L);

        // Assert
        assertSame(response, result);
        verify(caseRepository).findById(5L);
        verify(caseMapper).toCaseResponseDto(caseEntity);
    }

    @Test
    void shouldThrowCaseNotFoundException_whenGetCaseDoesNotFindCase() {
        // Arrange
        when(caseRepository.findById(5L)).thenReturn(Optional.empty());

        // Act
        CaseNotFoundException exception = assertThrows(CaseNotFoundException.class,
                () -> caseService.getCase(5L));

        // Assert
        assertEquals("Could not find case by id 5", exception.getMessage());
        verify(caseRepository).findById(5L);
        verify(caseMapper, never()).toCaseResponseDto(any(Case.class));
    }

    @Test
    void shouldReturnCases_whenSearchByPatientFindsCases() {
        // Arrange
        Case firstCase = ptCase(1L, 10L, 20L);
        Case secondCase = ptCase(2L, 10L, 21L);
        CaseResponseDto firstResponse = response(1L, 10L, 20L);
        CaseResponseDto secondResponse = response(2L, 10L, 21L);

        when(caseRepository.getCaseByPatient(10L)).thenReturn(List.of(firstCase, secondCase));
        when(caseMapper.toCaseResponseDto(firstCase)).thenReturn(firstResponse);
        when(caseMapper.toCaseResponseDto(secondCase)).thenReturn(secondResponse);

        // Act
        List<CaseResponseDto> result = caseService.searchByPatient(10L);

        // Assert
        assertEquals(List.of(firstResponse, secondResponse), result);
        verify(caseRepository).getCaseByPatient(10L);
        verify(caseMapper).toCaseResponseDto(firstCase);
        verify(caseMapper).toCaseResponseDto(secondCase);
    }

    @Test
    void shouldCreateCase_whenCreateCaseReceivesValidRequest() {
        // Arrange
        CaseRequestDto request = request(10L, 20L);
        Patient patient = patient(10L);
        BodyRegion bodyRegion = bodyRegion(20L);
        Case mappedCase = new Case();
        Case savedCase = ptCase(7L, 10L, 20L);
        CaseResponseDto response = response(7L, 10L, 20L);

        when(patientRepository.getPatientById(10L)).thenReturn(patient);
        when(bodyRegionRepository.findById(20L)).thenReturn(Optional.of(bodyRegion));
        when(caseMapper.toCase(request)).thenReturn(mappedCase);
        when(caseRepository.save(mappedCase)).thenReturn(savedCase);
        when(caseMapper.toCaseResponseDto(savedCase)).thenReturn(response);

        // Act
        CaseResponseDto result = caseService.createCase(request);

        // Assert
        assertSame(response, result);
        assertSame(patient, mappedCase.getPatient());
        assertSame(bodyRegion, mappedCase.getBodyRegion());
        assertNotNull(mappedCase.getCreatedAt());
        verify(patientRepository).getPatientById(10L);
        verify(bodyRegionRepository).findById(20L);
        verify(caseMapper).toCase(request);
        verify(caseRepository).save(mappedCase);
        verify(caseMapper).toCaseResponseDto(savedCase);
    }

    @Test
    void shouldThrowPatientNotFoundException_whenCreateCaseDoesNotFindPatient() {
        // Arrange
        CaseRequestDto request = request(10L, 20L);
        when(patientRepository.getPatientById(10L)).thenReturn(null);

        // Act
        PatientNotFoundException exception = assertThrows(PatientNotFoundException.class,
                () -> caseService.createCase(request));

        // Assert
        assertEquals("Patient with id 10 does not exist", exception.getMessage());
        verify(patientRepository).getPatientById(10L);
        verify(bodyRegionRepository, never()).findById(any(Long.class));
        verify(caseRepository, never()).save(any(Case.class));
    }

    @Test
    void shouldThrowBodyRegionNotFoundException_whenCreateCaseDoesNotFindBodyRegion() {
        // Arrange
        CaseRequestDto request = request(10L, 20L);
        when(patientRepository.getPatientById(10L)).thenReturn(patient(10L));
        when(bodyRegionRepository.findById(20L)).thenReturn(Optional.empty());

        // Act
        BodyRegionNotFoundException exception = assertThrows(BodyRegionNotFoundException.class,
                () -> caseService.createCase(request));

        // Assert
        assertEquals("Could not find body region with id 20", exception.getMessage());
        verify(patientRepository).getPatientById(10L);
        verify(bodyRegionRepository).findById(20L);
        verify(caseRepository, never()).save(any(Case.class));
    }

    @Test
    void shouldUpdateCase_whenUpdateCaseReceivesValidPatch() {
        // Arrange
        Case caseEntity = ptCase(6L, 10L, 20L);
        BodyRegion newBodyRegion = bodyRegion(30L);
        CasePatchDto patch = patch(30L);
        CaseResponseDto response = response(6L, 10L, 30L);

        when(caseRepository.findById(6L)).thenReturn(Optional.of(caseEntity));
        when(bodyRegionRepository.findById(30L)).thenReturn(Optional.of(newBodyRegion));
        when(caseRepository.save(caseEntity)).thenReturn(caseEntity);
        when(caseMapper.toCaseResponseDto(caseEntity)).thenReturn(response);

        // Act
        CaseResponseDto result = caseService.updateCase(6L, patch);

        // Assert
        assertSame(response, result);
        assertSame(newBodyRegion, caseEntity.getBodyRegion());
        verify(caseRepository).findById(6L);
        verify(bodyRegionRepository).findById(30L);
        verify(caseRepository).save(caseEntity);
        verify(caseMapper).toCaseResponseDto(caseEntity);
    }

    @Test
    void shouldKeepExistingRegion_whenUpdateCaseReceivesNullPatch() {
        // Arrange
        Case caseEntity = ptCase(6L, 10L, 20L);
        CaseResponseDto response = response(6L, 10L, 20L);

        when(caseRepository.findById(6L)).thenReturn(Optional.of(caseEntity));
        when(caseRepository.save(caseEntity)).thenReturn(caseEntity);
        when(caseMapper.toCaseResponseDto(caseEntity)).thenReturn(response);

        // Act
        CaseResponseDto result = caseService.updateCase(6L, null);

        // Assert
        assertSame(response, result);
        assertEquals(20L, caseEntity.getBodyRegion().getId());
        verify(caseRepository).findById(6L);
        verify(bodyRegionRepository, never()).findById(any(Long.class));
        verify(caseRepository).save(caseEntity);
    }

    @Test
    void shouldThrowCaseNotFoundException_whenUpdateCaseDoesNotFindCase() {
        // Arrange
        when(caseRepository.findById(6L)).thenReturn(Optional.empty());

        // Act
        CaseNotFoundException exception = assertThrows(CaseNotFoundException.class,
                () -> caseService.updateCase(6L, patch(30L)));

        // Assert
        assertEquals("Could not find case by id 6", exception.getMessage());
        verify(caseRepository).findById(6L);
        verify(caseRepository, never()).save(any(Case.class));
    }

    @Test
    void shouldThrowBodyRegionNotFoundException_whenUpdateCaseDoesNotFindBodyRegion() {
        // Arrange
        Case caseEntity = ptCase(6L, 10L, 20L);
        when(caseRepository.findById(6L)).thenReturn(Optional.of(caseEntity));
        when(bodyRegionRepository.findById(30L)).thenReturn(Optional.empty());

        // Act
        BodyRegionNotFoundException exception = assertThrows(BodyRegionNotFoundException.class,
                () -> caseService.updateCase(6L, patch(30L)));

        // Assert
        assertEquals("Could not find body region with id 30", exception.getMessage());
        verify(caseRepository).findById(6L);
        verify(bodyRegionRepository).findById(30L);
        verify(caseRepository, never()).save(any(Case.class));
    }

    @Test
    void shouldDeleteCase_whenDeleteCaseIsCalled() {
        // Arrange

        // Act
        caseService.deleteCase(8L);

        // Assert
        verify(caseRepository).deleteById(8L);
    }

    private CaseRequestDto request(Long patientId, Long bodyRegionId) {
        CaseRequestDto request = new CaseRequestDto();
        request.setPatientId(patientId);
        request.setBodyRegionId(bodyRegionId);
        return request;
    }

    private CasePatchDto patch(Long bodyRegionId) {
        CasePatchDto patch = new CasePatchDto();
        patch.setBodyRegionId(bodyRegionId);
        return patch;
    }

    private Case ptCase(Long id, Long patientId, Long bodyRegionId) {
        Case caseEntity = new Case();
        caseEntity.setId(id);
        caseEntity.setCreatedAt(Date.from(Instant.parse("2026-03-01T10:00:00Z")));
        caseEntity.setPatient(patient(patientId));
        caseEntity.setBodyRegion(bodyRegion(bodyRegionId));
        return caseEntity;
    }

    private Patient patient(Long id) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setFirstName("Sam");
        patient.setLastName("Lee");
        return patient;
    }

    private BodyRegion bodyRegion(Long id) {
        BodyRegion bodyRegion = new BodyRegion();
        bodyRegion.setId(id);
        bodyRegion.setCode("SHOULDER");
        bodyRegion.setDisplayName("Shoulder");
        bodyRegion.setIsActive(true);
        return bodyRegion;
    }

    private CaseResponseDto response(Long id, Long patientId, Long bodyRegionId) {
        CaseResponseDto response = new CaseResponseDto();
        response.setId(id);
        response.setPatientId(patientId);
        response.setBodyRegionId(bodyRegionId);
        response.setCreatedAt(Date.from(Instant.parse("2026-03-01T10:00:00Z")));
        return response;
    }
}
