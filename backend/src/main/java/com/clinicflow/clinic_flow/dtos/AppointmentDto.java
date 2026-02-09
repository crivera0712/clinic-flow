package com.clinicflow.clinic_flow.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

@AllArgsConstructor
@Getter
public class AppointmentDto {

    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime scheduledAt;
    private String patient_name;
    private String bodyRegion;
    private String therapistName;
//     @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
//    private LocalDateTime createdAt;
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
//    private LocalDateTime modifiedAt;
}
