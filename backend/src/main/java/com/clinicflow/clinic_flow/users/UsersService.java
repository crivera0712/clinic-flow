package com.clinicflow.clinic_flow.users;

import com.clinicflow.clinic_flow.users.dtos.CreateUserRequest;
import com.clinicflow.clinic_flow.users.dtos.UsersResponseDto;
import com.clinicflow.clinic_flow.exception.UserAlreadyExistsException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;

    public List<UsersResponseDto> getUsers(){
        return usersRepository.findAll()
                .stream()
                .map(usersMapper::toUsersResponseDto)
                .toList();
    }

    public UsersResponseDto getUserById(Long id){
        return usersMapper.toUsersResponseDto(usersRepository.findById(id).orElseThrow());
    }

    public UsersResponseDto createUser(CreateUserRequest request){
        Optional<Users> userCheck = usersRepository.findByUsername(request.getUsername());
        if (userCheck.isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        Users user = usersMapper.toEntity(request);
        user.setCreatedAt(LocalDateTime.now());
        user.setRoleName(Users.RoleName.DISPLAY);
        var newUser = usersRepository.save(user);
        return usersMapper.toUsersResponseDto(newUser);
    }
}
