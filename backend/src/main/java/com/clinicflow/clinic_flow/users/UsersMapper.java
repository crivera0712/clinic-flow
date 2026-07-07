package com.clinicflow.clinic_flow.users;

import com.clinicflow.clinic_flow.auth.dtos.LoginResponse;
import com.clinicflow.clinic_flow.users.dtos.CreateUserRequest;
import com.clinicflow.clinic_flow.users.dtos.UsersResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsersMapper {

    public UsersResponseDto toUsersResponseDto(Users users);

    public Users toEntity(CreateUserRequest createUserRequest);

    @Mapping(target = "isDemo", source = "clinic.isDemo")
    public LoginResponse toLoginResponse(Users user);
}
