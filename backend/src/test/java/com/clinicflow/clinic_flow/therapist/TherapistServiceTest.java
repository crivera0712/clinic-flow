package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.exception.TherapistNotFoundException;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistPatchDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
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

    @InjectMocks
    private TherapistService therapistService;

    @Test
    void shouldReturnAllTherapists_whenSearchIsNull() {
        Therapist therapist = therapist(1L, "Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        TherapistsResponseDto response = response(1L, "Taylor", "Physical Therapist");
        Pageable pageable = PageRequest.of(0, 10);

        when(therapistRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(therapist)));
        when(therapistMapper.toTherapistResponseDto(therapist)).thenReturn(response);

        Page<TherapistsResponseDto> result = therapistService.getTherapists(null, pageable);

        assertEquals(List.of(response), result.getContent());
        verify(therapistRepository).findAll(pageable);
        verify(therapistRepository, never()).search(any(), eq(pageable));
    }

    @Test
    void shouldReturnAllTherapists_whenSearchIsBlank() {
        Therapist therapist = therapist(1L, "Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        TherapistsResponseDto response = response(1L, "Taylor", "Physical Therapist");
        Pageable pageable = PageRequest.of(0, 10);

        when(therapistRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(therapist)));
        when(therapistMapper.toTherapistResponseDto(therapist)).thenReturn(response);

        Page<TherapistsResponseDto> result = therapistService.getTherapists("   ", pageable);

        assertEquals(List.of(response), result.getContent());
        verify(therapistRepository).findAll(pageable);
        verify(therapistRepository, never()).search(any(), eq(pageable));
    }

    @Test
    void shouldTrimSearchBeforeRepositoryLookup() {
        Therapist therapist = therapist(1L, "Sam Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        TherapistsResponseDto response = response(1L, "Sam Taylor", "Physical Therapist");
        Pageable pageable = PageRequest.of(0, 10);

        when(therapistRepository.search("sam", pageable)).thenReturn(new PageImpl<>(List.of(therapist)));
        when(therapistMapper.toTherapistResponseDto(therapist)).thenReturn(response);

        Page<TherapistsResponseDto> result = therapistService.getTherapists(" sam ", pageable);

        assertEquals(List.of(response), result.getContent());
        verify(therapistRepository).search("sam", pageable);
        verify(therapistRepository, never()).findAll(pageable);
    }

    @Test
    void shouldReturnTherapistById_whenTherapistExists() {
        Therapist therapist = therapist(5L, "Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        TherapistsResponseDto response = response(5L, "Taylor", "Physical Therapist");

        when(therapistRepository.findById(5L)).thenReturn(Optional.of(therapist));
        when(therapistMapper.toTherapistResponseDto(therapist)).thenReturn(response);

        TherapistsResponseDto result = therapistService.getTherapistById(5L);

        assertSame(response, result);
        verify(therapistRepository).findById(5L);
    }

    @Test
    void shouldThrowWhenTherapistByIdMissing() {
        when(therapistRepository.findById(5L)).thenReturn(Optional.empty());

        TherapistNotFoundException exception = assertThrows(TherapistNotFoundException.class,
                () -> therapistService.getTherapistById(5L));

        assertEquals("Therapist with id 5 not found", exception.getMessage());
    }

    @Test
    void shouldUpdateTherapistNameOnly() {
        Therapist therapist = therapist(9L, "Taylor", Therapist.TherapistType.PHYSICAL_THERAPIST);
        TherapistPatchDto patch = new TherapistPatchDto();
        patch.setTherapistName("Jordan");
        TherapistsResponseDto response = response(9L, "Jordan", "Physical Therapist");

        when(therapistRepository.findById(9L)).thenReturn(Optional.of(therapist));
        when(therapistRepository.save(therapist)).thenReturn(therapist);
        when(therapistMapper.toTherapistResponseDto(therapist)).thenReturn(response);

        TherapistsResponseDto result = therapistService.updateTherapist(9L, patch);

        assertSame(response, result);
        assertEquals("Jordan", therapist.getTherapistName());
        assertEquals(Therapist.TherapistType.PHYSICAL_THERAPIST, therapist.getType());
    }

    @Test
    void shouldReturnEmptyPage_whenNoTherapistsFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(therapistRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        Page<TherapistsResponseDto> result = therapistService.getTherapists(null, pageable);

        assertTrue(result.isEmpty());
        verify(therapistMapper, never()).toTherapistResponseDto(any());
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
}
