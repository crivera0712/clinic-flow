package com.clinicflow.clinic_flow.users;

import com.clinicflow.clinic_flow.exception.UserNotFoundException;
import com.clinicflow.clinic_flow.users.dtos.*;
import com.clinicflow.clinic_flow.exception.UserAlreadyExistsException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class UsersService implements UserDetailsService {
    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final PasswordEncoder passwordEncoder;


    public List<UsersResponseDto> getUsers(){
        return usersRepository.findAll()
                .stream()
                .map(usersMapper::toUsersResponseDto)
                .toList();
    }

    public UsersResponseDto getUserById(Long id){
        return usersMapper.toUsersResponseDto(
                usersRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id))
        );
    }

    @Transactional
    public UsersResponseDto createUser(@NonNull CreateUserRequest request){
        Optional<Users> userCheck = usersRepository.findByUsername(request.getUsername());
        if (userCheck.isPresent()) {
            throw new UserAlreadyExistsException(request.getUsername());
        }

        Users user = usersMapper.toEntity(request);

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRoleName(Users.RoleName.DISPLAY);
        user.setEnabled(Boolean.TRUE);

        var newUser = usersRepository.save(user);

        return usersMapper.toUsersResponseDto(newUser);
    }

    @Transactional
    public UsersResponseDto updateUser(Long id, @NonNull UserPatchDto patch) {
        var user = usersRepository.findById(id).orElseThrow( () ->
        new UserNotFoundException(id));

        user.setRoleName(patch.getRoleName());

        return usersMapper.toUsersResponseDto(usersRepository.save(user));

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user =  usersRepository.findByUsername(username).orElseThrow( () ->
                new UsernameNotFoundException(username));

        return new User(
                user.getUsername(),
                user.getPasswordHash(),
                Collections.emptyList()
        );
    }
}
