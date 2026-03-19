package com.clinicflow.clinic_flow.mappers;

import com.clinicflow.clinic_flow.dtos.users.CreateUserRequest;
import com.clinicflow.clinic_flow.dtos.users.UsersResponseDto;
import com.clinicflow.clinic_flow.entity.Users;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsersMapper {
    public UsersResponseDto toUsersResponseDto(Users users);
    public Users toEntity(CreateUserRequest createUserRequest);
}
