package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.exception.TherapistNotFoundException;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistRequestDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import com.clinicflow.clinic_flow.users.CurrentUserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class TherapistService {
    private final TherapistRepository therapistRepository;
    private final TherapistMapper therapistMapper;
    private final CurrentUserService currentUserService;
    private final ClinicContextService clinicContextService;

    public List<TherapistsResponseDto> getTherapists() {
        var clinicId = currentUserService.getCurrentClinicId();
        return therapistRepository.findAllByClinicId(clinicId)
                .stream()
                .map(therapistMapper::toTherapistResponseDto)
                .toList();
    }

    public TherapistsResponseDto getTherapistById(Long id) {
        var clinicId = currentUserService.getCurrentClinicId();
        return therapistMapper.toTherapistResponseDto(findTherapistOrThrow(id, clinicId));
    }

    @Transactional
    public TherapistsResponseDto createTherapist(TherapistRequestDto therapistRequestDto) {
        var clinic = clinicContextService.requireWritableClinic();
        var therapist = therapistMapper.toTherapist(therapistRequestDto);
        therapist.setClinic(clinic);
        return therapistMapper.toTherapistResponseDto(therapistRepository.save(therapist));
    }

    @Transactional
    public void deleteTherapist(Long id) {
        clinicContextService.assertWritableClinic();
        var clinicId = currentUserService.getCurrentClinicId();
        therapistRepository.delete(findTherapistOrThrow(id, clinicId));
    }

    private Therapist findTherapistOrThrow(Long id, Long clinicId) {
        return therapistRepository.findByIdAndClinicId(id, clinicId).orElseThrow(
                () -> new TherapistNotFoundException(id));
    }
}
