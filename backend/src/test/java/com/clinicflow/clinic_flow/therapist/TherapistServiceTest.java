package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.clinics.Clinics;
import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.exception.TherapistNotFoundException;
import com.clinicflow.clinic_flow.exception.DemoClinicReadOnlyException;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistPatchDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistRequestDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import com.clinicflow.clinic_flow.users.CurrentUserService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TherapistServiceTest {

    @Mock
    private TherapistRepository therapistRepository;

    @Mock
    private TherapistMapper therapistMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ClinicContextService clinicContextService;

    @InjectMocks
    private TherapistService therapistService;

    @Test
    void shouldReturnAllTherapistsWithinCurrentClinic() {
        Therapist therapist = therapist(1L, "Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        TherapistsResponseDto response = response(1L, "Taylor", "Physical Therapist");
        Pageable pageable = PageRequest.of(0, 10);

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(therapistRepository.findAllByClinicId(7L, pageable)).thenReturn(new PageImpl<>(List.of(therapist)));
        when(therapistMapper.toTherapistResponseDto(therapist)).thenReturn(response);

        Page<TherapistsResponseDto> result = therapistService.getTherapists(null, pageable);

        assertEquals(List.of(response), result.getContent());
        verify(therapistRepository).findAllByClinicId(7L, pageable);
        verify(therapistRepository, never()).search(any(), eq(pageable), any());
    }

    @Test
    void shouldTrimSearchBeforeRepositoryLookup() {
        Therapist therapist = therapist(1L, "Sam Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        TherapistsResponseDto response = response(1L, "Sam Taylor", "Physical Therapist");
        Pageable pageable = PageRequest.of(0, 10);

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(therapistRepository.search("sam", pageable, 7L)).thenReturn(new PageImpl<>(List.of(therapist)));
        when(therapistMapper.toTherapistResponseDto(therapist)).thenReturn(response);

        Page<TherapistsResponseDto> result = therapistService.getTherapists(" sam ", pageable);

        assertEquals(List.of(response), result.getContent());
        verify(therapistRepository).search("sam", pageable, 7L);
    }

    @Test
    void shouldReturnTherapistByIdWithinClinic() {
        Therapist therapist = therapist(5L, "Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        TherapistsResponseDto response = response(5L, "Taylor", "Physical Therapist");

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(therapistRepository.findByIdAndClinicId(5L, 7L)).thenReturn(Optional.of(therapist));
        when(therapistMapper.toTherapistResponseDto(therapist)).thenReturn(response);

        TherapistsResponseDto result = therapistService.getTherapistById(5L);

        assertSame(response, result);
        verify(therapistRepository).findByIdAndClinicId(5L, 7L);
    }

    @Test
    void shouldCreateTherapistWithinCurrentClinic() {
        TherapistRequestDto request = new TherapistRequestDto("Jordan", Therapist.TherapistType.PHYSICAL_THERAPIST);
        Therapist mapped = therapist(null, "Jordan", Therapist.TherapistType.PHYSICAL_THERAPIST);
        Therapist saved = therapist(3L, "Jordan", Therapist.TherapistType.PHYSICAL_THERAPIST);
        TherapistsResponseDto response = response(3L, "Jordan", "Physical Therapist");
        Clinics clinic = clinic(7L);

        when(clinicContextService.requireWritableClinic()).thenReturn(clinic);
        when(therapistMapper.toTherapist(request)).thenReturn(mapped);
        when(therapistRepository.save(mapped)).thenReturn(saved);
        when(therapistMapper.toTherapistResponseDto(saved)).thenReturn(response);

        TherapistsResponseDto result = therapistService.createTherapist(request);

        assertSame(response, result);
        assertSame(clinic, mapped.getClinic());
    }

    @Test
    void shouldThrowWhenTherapistIsMissing() {
        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(therapistRepository.findByIdAndClinicId(5L, 7L)).thenReturn(Optional.empty());

        TherapistNotFoundException exception = assertThrows(
                TherapistNotFoundException.class,
                () -> therapistService.getTherapistById(5L)
        );

        assertEquals("Therapist with id 5 not found", exception.getMessage());
    }

    @Test
    void shouldThrowWhenUpdateTargetsTherapistFromAnotherClinic() {
        TherapistPatchDto patch = new TherapistPatchDto();
        patch.setTherapistName("Jordan");

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(therapistRepository.findByIdAndClinicId(5L, 7L)).thenReturn(Optional.empty());

        assertThrows(TherapistNotFoundException.class, () -> therapistService.updateTherapist(5L, patch));

        verify(therapistRepository, never()).save(any(Therapist.class));
    }

    @Test
    void shouldThrowWhenDeleteTargetsTherapistFromAnotherClinic() {
        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(therapistRepository.findByIdAndClinicId(5L, 7L)).thenReturn(Optional.empty());

        assertThrows(TherapistNotFoundException.class, () -> therapistService.deleteTherapist(5L));

        verify(therapistRepository, never()).delete(any(Therapist.class));
    }

    @Test
    void shouldThrowWhenDemoClinicCreatesTherapist() {
        TherapistRequestDto request = new TherapistRequestDto("Jordan", Therapist.TherapistType.PHYSICAL_THERAPIST);

        when(clinicContextService.requireWritableClinic()).thenThrow(new DemoClinicReadOnlyException());

        assertThrows(DemoClinicReadOnlyException.class, () -> therapistService.createTherapist(request));

        verify(therapistRepository, never()).save(any(Therapist.class));
    }

    @Test
    void shouldThrowWhenDemoClinicUpdatesTherapist() {
        org.mockito.Mockito.doThrow(new DemoClinicReadOnlyException()).when(clinicContextService).assertWritableClinic();

        TherapistPatchDto patch = new TherapistPatchDto();
        patch.setTherapistName("Jordan");

        assertThrows(DemoClinicReadOnlyException.class, () -> therapistService.updateTherapist(5L, patch));

        verify(therapistRepository, never()).findByIdAndClinicId(any(), any());
    }

    @Test
    void shouldThrowWhenDemoClinicDeletesTherapist() {
        org.mockito.Mockito.doThrow(new DemoClinicReadOnlyException()).when(clinicContextService).assertWritableClinic();

        assertThrows(DemoClinicReadOnlyException.class, () -> therapistService.deleteTherapist(5L));

        verify(therapistRepository, never()).delete(any(Therapist.class));
    }

    private Therapist therapist(Long id, String name, Therapist.TherapistType type) {
        Therapist therapist = new Therapist();
        therapist.setId(id);
        therapist.setTherapistName(name);
        therapist.setType(type);
        return therapist;
    }

    private TherapistsResponseDto response(Long id, String name, String type) {
        return new TherapistsResponseDto(id, name, type);
    }

    private Clinics clinic(Long id) {
        Clinics clinic = new Clinics();
        clinic.setId(id);
        clinic.setSlug("clinic-" + id);
        return clinic;
    }
}
