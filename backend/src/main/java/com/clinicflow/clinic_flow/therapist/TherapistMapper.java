package com.clinicflow.clinic_flow.therapist;

import com.clinicflow.clinic_flow.therapist.dtos.TherapistRequestDto;
import com.clinicflow.clinic_flow.therapist.dtos.TherapistsResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TherapistMapper {
    @Mapping(target = "therapistId", source = "id")
    TherapistsResponseDto toTherapistResponseDto(Therapist therapist);
    Therapist toTherapist(TherapistRequestDto therapistRequestDto);

    default String map(Therapist.TherapistType type) {
        return type == null ? null : type.getDisplayName();
    }
}
