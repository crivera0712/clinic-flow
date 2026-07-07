package com.clinicflow.clinic_flow.users;

import com.clinicflow.clinic_flow.clinics.ClinicContextService;
import com.clinicflow.clinic_flow.exception.UserAlreadyExistsException;
import com.clinicflow.clinic_flow.exception.UserNotFoundException;
import com.clinicflow.clinic_flow.users.dtos.*;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UsersService implements UserDetailsService {
    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final ClinicContextService clinicContextService;

    public List<UsersResponseDto> getUsers() {
        var clinicId = currentUserService.getCurrentClinicId();
        return usersRepository.findAllByClinicId(clinicId).stream()
                .map(usersMapper::toUsersResponseDto)
                .toList();
    }

    public UsersResponseDto getUserById(Long id) {
        var clinicId = currentUserService.getCurrentClinicId();
        return usersMapper.toUsersResponseDto(
                usersRepository.findByIdAndClinicId(id, clinicId).orElseThrow(() -> new UserNotFoundException(id)));
    }

    @Transactional
    public UsersResponseDto createUser(@NonNull CreateUserRequest request) {
        var clinic = clinicContextService.requireWritableClinic();
        var clinicId = clinic.getId();
        Optional<Users> userCheck = usersRepository.findByUsernameAndClinicId(request.getUsername(), clinicId);
        if (userCheck.isPresent()) {
            throw new UserAlreadyExistsException(request.getUsername());
        }

        Users user = usersMapper.toEntity(request);

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRoleName(Users.RoleName.DISPLAY);
        user.setEnabled(Boolean.TRUE);
        user.setClinic(clinic);

        var newUser = usersRepository.save(user);

        return usersMapper.toUsersResponseDto(newUser);
    }

    @Transactional
    public UsersResponseDto updateUser(Long id, @NonNull UserPatchDto patch) {
        var clinicId = currentUserService.getCurrentClinicId();
        var user = usersRepository.findByIdAndClinicId(id, clinicId).orElseThrow(() -> new UserNotFoundException(id));

        user.setRoleName(patch.getRoleName());

        return usersMapper.toUsersResponseDto(usersRepository.save(user));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = usersRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));

        return new User(user.getUsername(), user.getPasswordHash(), Collections.emptyList());
    }
}
