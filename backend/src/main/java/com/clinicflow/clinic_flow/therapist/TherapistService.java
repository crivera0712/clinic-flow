package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.exception.TherapistNotFoundException;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistPatchDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistRequestDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import com.clinicflow.clinic_flow.users.CurrentUserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class TherapistService {
    private final TherapistRepository therapistRepository;
    private final TherapistMapper therapistMapper;
    private final CurrentUserService currentUserService;
    private final ClinicContextService clinicContextService;

    public Page<TherapistsResponseDto> getTherapists(String search, Pageable pageable){
        var clinicId = currentUserService.getCurrentClinicId();

        var therapists = search == null || search.isBlank()
                ? therapistRepository.findAllByClinicId(clinicId, pageable)
                : therapistRepository.search(search.trim(), pageable, clinicId);

        return therapists
                .map(therapistMapper::toTherapistResponseDto);
    }

    public TherapistsResponseDto getTherapistById(Long id){
        var clinicId = currentUserService.getCurrentClinicId();

        return therapistMapper.toTherapistResponseDto(findTherapistOrThrow(id, clinicId));
    }

    @Transactional
    public TherapistsResponseDto createTherapist(TherapistRequestDto therapistRequestDto){
        var clinic = clinicContextService.requireWritableClinic();

        var therapist = therapistMapper.toTherapist(therapistRequestDto);
        therapist.setClinic(clinic);
        var response = therapistRepository.save(therapist);
        return therapistMapper.toTherapistResponseDto(response);
    }

    @Transactional
    public TherapistsResponseDto updateTherapist(Long id, TherapistPatchDto patch){
        clinicContextService.assertWritableClinic();
        var clinicId = currentUserService.getCurrentClinicId();

        var therapist = findTherapistOrThrow(id, clinicId);

        if (patch == null){
            return therapistMapper.toTherapistResponseDto(therapist);
        }


        if (patch.getTherapistName() != null && !patch.getTherapistName().isEmpty()) {
            String name = patch.getTherapistName().trim();
            if (!name.isEmpty() && therapistRepository.searchByTherapistNameAndClinicId(name, clinicId).isEmpty()) {
                therapist.setTherapistName(name);
            }
        }

        if (patch.getTherapistType() != null){
            therapist.setType(patch.getTherapistType());
        }

        therapistRepository.save(therapist);

        return therapistMapper.toTherapistResponseDto(therapist);
    }

    @Transactional
    public void deleteTherapist(Long id){
        clinicContextService.assertWritableClinic();
        var clinicId = currentUserService.getCurrentClinicId();

        var therapist = findTherapistOrThrow(id, clinicId);

        therapistRepository.delete(therapist);
    }

    private Therapist findTherapistOrThrow(Long id, Long clinicId){
        return  therapistRepository.findByIdAndClinicId(id, clinicId).orElseThrow(
                () -> new TherapistNotFoundException(id)
        );
    }


}
