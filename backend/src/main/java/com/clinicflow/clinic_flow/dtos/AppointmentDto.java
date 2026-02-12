package com.clinicflow.clinic_flow.dtos;

import com.clinicflow.clinic_flow.entity.Case;
import com.clinicflow.clinic_flow.entity.Therapist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class AppointmentDto {
    private Long  id;
    private LocalDateTime scheduledAt;
    private Instant createdAt;
    private Instant modifiedAt;
    private Case ptCase;
    private Therapist therapist;


}
