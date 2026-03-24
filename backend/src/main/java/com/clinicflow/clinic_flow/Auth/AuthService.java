package com.clinicflow.clinic_flow.Auth;

import com.clinicflow.clinic_flow.Auth.dtos.LoginRequest;
import com.clinicflow.clinic_flow.Auth.dtos.LoginResponse;
import com.clinicflow.clinic_flow.exception.InvalidCredentialsException;
import com.clinicflow.clinic_flow.exception.UserNotFoundException;
import com.clinicflow.clinic_flow.users.UsersMapper;
import com.clinicflow.clinic_flow.users.UsersRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersMapper usersMapper;

    public void loginUser(@NonNull LoginRequest request){
        var user = usersRepository.findByUsername(request.getUsername()).orElseThrow( () ->
                new UserNotFoundException(request.getUsername()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
    }
}
