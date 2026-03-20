package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.therapist.dtos.TherapistRequestDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TherapistMapper {
    TherapistsResponseDto toTherapistResponseDto(Therapist therapist);
    Therapist toTherapist(TherapistRequestDto therapistRequestDto);

    default String map(Therapist.TherapistType type) {
        return type == null ? null : type.getDisplayName();
    }
}
