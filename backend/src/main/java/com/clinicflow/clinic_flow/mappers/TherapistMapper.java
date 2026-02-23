package com.clinicflow.clinic_flow.mappers;

import com.clinicflow.clinic_flow.dtos.therapists.TherapistRequestDto;
import com.clinicflow.clinic_flow.dtos.therapists.TherapistsResponseDto;
import com.clinicflow.clinic_flow.entity.Therapist;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TherapistMapper {
    TherapistsResponseDto toTherapistResponseDto(Therapist therapist);
    Therapist toTherapist(TherapistRequestDto therapistRequestDto);

    default String map(Therapist.TherapistType type) {
        return type == null ? null : type.getDisplayName();
    }
}
