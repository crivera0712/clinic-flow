package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.therapist.dtos.TherapistRequestDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TherapistMapper {

    @Mapping(target = "name", source = "therapistName")
    TherapistsResponseDto toTherapistResponseDto(Therapist therapist);

    @Mapping(target = "therapistName", source = "name")
    Therapist toTherapist(TherapistRequestDto therapistRequestDto);
}
