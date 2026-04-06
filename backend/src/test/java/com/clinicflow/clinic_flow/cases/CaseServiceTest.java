package com.clinicflow.clinic_flow.cases;

import com.clinicflow.clinic_flow.body_region.BodyRegion;
import com.clinicflow.clinic_flow.body_region.BodyRegionRepository;
import com.clinicflow.clinic_flow.cases.dtos.CasePatchDto;
import com.clinicflow.clinic_flow.cases.dtos.CaseRequestDto;
import com.clinicflow.clinic_flow.cases.dtos.CaseResponseDto;
import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.exception.DemoClinicReadOnlyException;
import com.clinicflow.clinic_flow.exception.CaseNotFoundException;
import com.clinicflow.clinic_flow.exception.PatientNotFoundException;
import com.clinicflow.clinic_flow.patient.Patient;
import com.clinicflow.clinic_flow.patient.PatientRepository;
import com.clinicflow.clinic_flow.users.CurrentUserService;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private BodyRegionRepository bodyRegionRepository;

    @Mock
    private ClinicContextService clinicContextService;

    @InjectMocks
    private CaseService caseService;

    @Test
    void shouldReturnCasesWithinCurrentClinic() {
        Case firstCase = ptCase(1L, 10L, 20L);
        Case secondCase = ptCase(2L, 11L, 21L);
        CaseResponseDto firstResponse = response(1L, 10L, 20L);
        CaseResponseDto secondResponse = response(2L, 11L, 21L);

        when(currentUserService.getCurrentClinicId()).thenReturn(1L);
        when(caseRepository.findAllByClinicId(1L)).thenReturn(List.of(firstCase, secondCase));
        when(caseMapper.toCaseResponseDto(firstCase)).thenReturn(firstResponse);
        when(caseMapper.toCaseResponseDto(secondCase)).thenReturn(secondResponse);

        List<CaseResponseDto> result = caseService.getCases();

        assertEquals(List.of(firstResponse, secondResponse), result);
        verify(caseRepository).findAllByClinicId(1L);
    }

    @Test
    void shouldReturnCaseWithinCurrentClinic() {
        Case caseEntity = ptCase(5L, 10L, 20L);
        CaseResponseDto response = response(5L, 10L, 20L);

        when(currentUserService.getCurrentClinicId()).thenReturn(1L);
        when(caseRepository.findByIdAndClinicId(5L, 1L)).thenReturn(Optional.of(caseEntity));
        when(caseMapper.toCaseResponseDto(caseEntity)).thenReturn(response);

        CaseResponseDto result = caseService.getCase(5L);

        assertSame(response, result);
        verify(caseRepository).findByIdAndClinicId(5L, 1L);
    }

    @Test
    void shouldSearchByPatientWithinCurrentClinic() {
        Case firstCase = ptCase(1L, 10L, 20L);
        Case secondCase = ptCase(2L, 10L, 21L);
        CaseResponseDto firstResponse = response(1L, 10L, 20L);
        CaseResponseDto secondResponse = response(2L, 10L, 21L);

        when(currentUserService.getCurrentClinicId()).thenReturn(1L);
        when(caseRepository.getCaseByPatient(10L, 1L)).thenReturn(List.of(firstCase, secondCase));
        when(caseMapper.toCaseResponseDto(firstCase)).thenReturn(firstResponse);
        when(caseMapper.toCaseResponseDto(secondCase)).thenReturn(secondResponse);

        List<CaseResponseDto> result = caseService.searchByPatient(10L);

        assertEquals(List.of(firstResponse, secondResponse), result);
    }

    @Test
    void shouldCreateCaseWithinCurrentClinic() {
        CaseRequestDto request = request(10L, 20L);
        Patient patient = patient(10L);
        patient.setClinic(clinic(1L));
        BodyRegion bodyRegion = bodyRegion(20L);
        Case mappedCase = new Case();
        Case savedCase = ptCase(7L, 10L, 20L);
        CaseResponseDto response = response(7L, 10L, 20L);

        when(currentUserService.getCurrentClinicId()).thenReturn(1L);
        when(patientRepository.findByIdAndClinicId(10L, 1L)).thenReturn(Optional.of(patient));
        when(bodyRegionRepository.findById(20L)).thenReturn(Optional.of(bodyRegion));
        when(caseMapper.toCase(request)).thenReturn(mappedCase);
        when(caseRepository.save(mappedCase)).thenReturn(savedCase);
        when(caseMapper.toCaseResponseDto(savedCase)).thenReturn(response);

        CaseResponseDto result = caseService.createCase(request);

        assertSame(response, result);
        assertSame(patient, mappedCase.getPatient());
        assertSame(bodyRegion, mappedCase.getBodyRegion());
        assertSame(patient.getClinic(), mappedCase.getClinic());
    }

    @Test
    void shouldThrowPatientNotFoundWhenCreateCaseMissesClinicScopedPatient() {
        CaseRequestDto request = request(10L, 20L);

        when(currentUserService.getCurrentClinicId()).thenReturn(1L);
        when(patientRepository.findByIdAndClinicId(10L, 1L)).thenReturn(Optional.empty());

        PatientNotFoundException exception = assertThrows(
                PatientNotFoundException.class,
                () -> caseService.createCase(request)
        );

        assertEquals("Patient with id 10 does not exist", exception.getMessage());
        verify(bodyRegionRepository, never()).findById(any(Long.class));
    }

    @Test
    void shouldDeleteCaseWithinCurrentClinic() {
        Case caseEntity = ptCase(8L, 10L, 20L);

        when(currentUserService.getCurrentClinicId()).thenReturn(1L);
        when(caseRepository.findByIdAndClinicId(8L, 1L)).thenReturn(Optional.of(caseEntity));

        caseService.deleteCase(8L);

        verify(caseRepository).delete(caseEntity);
    }

    @Test
    void shouldThrowWhenDemoClinicCreatesCase() {
        org.mockito.Mockito.doThrow(new DemoClinicReadOnlyException()).when(clinicContextService).assertWritableClinic();

        assertThrows(DemoClinicReadOnlyException.class, () -> caseService.createCase(request(10L, 20L)));

        verify(patientRepository, never()).findByIdAndClinicId(any(), any());
    }

    @Test
    void shouldThrowWhenDemoClinicUpdatesCase() {
        org.mockito.Mockito.doThrow(new DemoClinicReadOnlyException()).when(clinicContextService).assertWritableClinic();

        assertThrows(DemoClinicReadOnlyException.class, () -> caseService.updateCase(8L, new CasePatchDto()));

        verify(caseRepository, never()).findByIdAndClinicId(any(), any());
    }

    @Test
    void shouldThrowWhenDemoClinicDeletesCase() {
        org.mockito.Mockito.doThrow(new DemoClinicReadOnlyException()).when(clinicContextService).assertWritableClinic();

        assertThrows(DemoClinicReadOnlyException.class, () -> caseService.deleteCase(8L));

        verify(caseRepository, never()).delete(any(Case.class));
    }

    @Test
    void shouldThrowWhenUpdateTargetsCaseFromAnotherClinic() {
        CasePatchDto patch = new CasePatchDto();
        patch.setBodyRegionId(30L);

        when(currentUserService.getCurrentClinicId()).thenReturn(1L);
        when(caseRepository.findByIdAndClinicId(8L, 1L)).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class, () -> caseService.updateCase(8L, patch));

        verify(bodyRegionRepository, never()).findById(any(Long.class));
        verify(caseRepository, never()).save(any(Case.class));
    }

    @Test
    void shouldThrowWhenDeleteTargetsCaseFromAnotherClinic() {
        when(currentUserService.getCurrentClinicId()).thenReturn(1L);
        when(caseRepository.findByIdAndClinicId(8L, 1L)).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class, () -> caseService.deleteCase(8L));

        verify(caseRepository, never()).delete(any(Case.class));
    }

    private CaseRequestDto request(Long patientId, Long bodyRegionId) {
        CaseRequestDto request = new CaseRequestDto();
        request.setPatientId(patientId);
        request.setBodyRegionId(bodyRegionId);
        return request;
    }

    private Case ptCase(Long id, Long patientId, Long bodyRegionId) {
        Case caseEntity = new Case();
        caseEntity.setId(id);
        caseEntity.setCreatedAt(Date.from(Instant.parse("2026-03-01T10:00:00Z")));
        caseEntity.setPatient(patient(patientId));
        caseEntity.setBodyRegion(bodyRegion(bodyRegionId));
        caseEntity.setClinic(clinic(1L));
        return caseEntity;
    }

    private Patient patient(Long id) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setFirstName("Sam");
        patient.setLastName("Lee");
        patient.setDisplayName("Lee, S");
        return patient;
    }

    private Clinics clinic(Long id) {
        Clinics clinic = new Clinics();
        clinic.setId(id);
        clinic.setSlug("clinic-" + id);
        return clinic;
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
