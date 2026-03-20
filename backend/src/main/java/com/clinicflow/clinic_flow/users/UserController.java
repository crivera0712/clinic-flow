package com.clinicflow.clinic_flow.users;

import com.clinicflow.clinic_flow.users.dtos.CreateUserRequest;
import com.clinicflow.clinic_flow.users.dtos.UsersResponseDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UsersService usersService;

    @GetMapping
    public List<UsersResponseDto> getUsers() {
        return usersService.getUsers();
    }

    @GetMapping("/{userId}")
    public UsersResponseDto getUser(@PathVariable Long userId) {
        return usersService.getUserById(userId);
    }

    @PostMapping()
    public ResponseEntity<UsersResponseDto> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {

        var createdUser = usersService.createUser(createUserRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();
        return ResponseEntity
                .created(location)
                .body(createdUser);
    }
}
