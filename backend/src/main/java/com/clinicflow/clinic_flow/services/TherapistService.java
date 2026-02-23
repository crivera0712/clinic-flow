package com.clinicflow.clinic_flow.services;

import com.clinicflow.clinic_flow.dtos.therapists.TherapistRequestDto;
import com.clinicflow.clinic_flow.dtos.therapists.TherapistsResponseDto;
import com.clinicflow.clinic_flow.mappers.TherapistMapper;
import com.clinicflow.clinic_flow.repositories.TherapistRepository;
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
        return therapistMapper.toTherapistResponseDto(therapistRepository.findById(id).orElseThrow());
    }

    public TherapistsResponseDto createTherapist(TherapistRequestDto therapistRequestDto){
        var therapist = therapistMapper.toTherapist(therapistRequestDto);
        var response = therapistRepository.save(therapist);
        return therapistMapper.toTherapistResponseDto(response);
    }


}
