package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.exception.TherapistNotFoundException;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistPatchDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistRequestDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class TherapistService {
    private final TherapistRepository therapistRepository;
    private final TherapistMapper therapistMapper;

    public List<TherapistsResponseDto> getTherapists(){
        return therapistRepository.findAll().stream().map(therapistMapper::toTherapistResponseDto).toList();
    }

    public TherapistsResponseDto getTherapistById(Long id){
        return therapistMapper.toTherapistResponseDto(therapistRepository.findById(id).orElseThrow( () ->
                new TherapistNotFoundException(id)));
    }

    @Transactional
    public TherapistsResponseDto createTherapist(TherapistRequestDto therapistRequestDto){
        var therapist = therapistMapper.toTherapist(therapistRequestDto);
        var response = therapistRepository.save(therapist);
        return therapistMapper.toTherapistResponseDto(response);
    }

    @Transactional
    public TherapistsResponseDto updateTherapist(Long id, TherapistPatchDto patch){
        var therapist = therapistRepository.findById(id).orElseThrow( () ->
                new TherapistNotFoundException(id));

        if (patch.getTherapistName() != null && !patch.getTherapistName().isEmpty()) {
            therapist.setTherapistName(patch.getTherapistName());
        }

        if (patch.getTherapistType() != null){
            therapist.setType(patch.getTherapistType());
        }

        therapistRepository.save(therapist);

        return therapistMapper.toTherapistResponseDto(therapist);
    }

    @Transactional
    public void deleteTherapist(Long id){

        var therapist = therapistRepository.findById(id).orElseThrow( () ->
        new TherapistNotFoundException(id));

        therapistRepository.delete(therapist);
    }


}
