package com.clinicflow.clinic_flow.auth;

import com.clinicflow.clinic_flow.auth.dtos.*;
import com.clinicflow.clinic_flow.exception.UserNotFoundException;
import com.clinicflow.clinic_flow.users.UsersMapper;
import com.clinicflow.clinic_flow.users.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;

    public LoginResult login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = usersRepository.findByUsername(request.getUsername()).orElseThrow( () ->
                new UserNotFoundException(request.getUsername()));

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return new LoginResult(accessToken, refreshToken);
    }

    public Jwt refreshToken(Jwt refreshToken) {
        var user = usersRepository.findById(refreshToken.getUserId()).orElseThrow();
        return jwtService.generateAccessToken(user);
    }

    public LoginResponse me() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var idAsString = authentication.getPrincipal().toString();
        var id = Long.parseLong(idAsString);

        System.out.println("id : " + id);

        var user = usersRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        return usersMapper.toLoginResponse(user);
    }


}
