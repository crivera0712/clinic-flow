package com.clinicflow.clinic_flow.therapist;

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
import com.clinicflow.clinic_flow.exception.TherapistNotFoundException;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistRequestDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import com.clinicflow.clinic_flow.users.CurrentUserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void getTherapists_returnsClinicTherapists() {
        Therapist therapist = therapist(1L, "Taylor");
        TherapistsResponseDto response = new TherapistsResponseDto(1L, "Taylor");

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(therapistRepository.findAllByClinicId(7L)).thenReturn(List.of(therapist));
        when(therapistMapper.toTherapistResponseDto(therapist)).thenReturn(response);

        List<TherapistsResponseDto> result = therapistService.getTherapists();

        assertEquals(List.of(response), result);
        verify(therapistRepository).findAllByClinicId(7L);
    }

    @Test
    void getTherapistById_returnsTherapist() {
        Therapist therapist = therapist(5L, "Taylor");
        TherapistsResponseDto response = new TherapistsResponseDto(5L, "Taylor");

        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(therapistRepository.findByIdAndClinicId(5L, 7L)).thenReturn(Optional.of(therapist));
        when(therapistMapper.toTherapistResponseDto(therapist)).thenReturn(response);

        assertSame(response, therapistService.getTherapistById(5L));
    }

    @Test
    void createTherapist_savesWithinCurrentClinic() {
        TherapistRequestDto request = new TherapistRequestDto("Jordan");
        Therapist mapped = therapist(null, "Jordan");
        Therapist saved = therapist(3L, "Jordan");
        TherapistsResponseDto response = new TherapistsResponseDto(3L, "Jordan");
        Clinics clinic = clinic(7L);

        when(clinicContextService.requireWritableClinic()).thenReturn(clinic);
        when(therapistMapper.toTherapist(request)).thenReturn(mapped);
        when(therapistRepository.save(mapped)).thenReturn(saved);
        when(therapistMapper.toTherapistResponseDto(saved)).thenReturn(response);

        assertSame(response, therapistService.createTherapist(request));
        assertSame(clinic, mapped.getClinic());
    }

    @Test
    void getTherapistById_whenMissing_throws() {
        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(therapistRepository.findByIdAndClinicId(5L, 7L)).thenReturn(Optional.empty());

        assertThrows(TherapistNotFoundException.class, () -> therapistService.getTherapistById(5L));
    }

    @Test
    void deleteTherapist_whenMissing_throws() {
        when(currentUserService.getCurrentClinicId()).thenReturn(7L);
        when(therapistRepository.findByIdAndClinicId(5L, 7L)).thenReturn(Optional.empty());

        assertThrows(TherapistNotFoundException.class, () -> therapistService.deleteTherapist(5L));
        verify(therapistRepository, never()).delete(any(Therapist.class));
    }

    @Test
    void createTherapist_inDemoClinic_throws() {
        when(clinicContextService.requireWritableClinic()).thenThrow(new DemoClinicReadOnlyException());

        assertThrows(
                DemoClinicReadOnlyException.class,
                () -> therapistService.createTherapist(new TherapistRequestDto("Jordan")));
        verify(therapistRepository, never()).save(any(Therapist.class));
    }

    @Test
    void deleteTherapist_inDemoClinic_throws() {
        org.mockito.Mockito.doThrow(new DemoClinicReadOnlyException())
                .when(clinicContextService)
                .assertWritableClinic();

        assertThrows(DemoClinicReadOnlyException.class, () -> therapistService.deleteTherapist(5L));
        verify(therapistRepository, never()).delete(any(Therapist.class));
    }

    private Therapist therapist(Long id, String name) {
        Therapist therapist = new Therapist();
        therapist.setId(id);
        therapist.setTherapistName(name);
        return therapist;
    }

    private Clinics clinic(Long id) {
        Clinics clinic = new Clinics();
        clinic.setId(id);
        clinic.setSlug("clinic-" + id);
        return clinic;
    }
}
