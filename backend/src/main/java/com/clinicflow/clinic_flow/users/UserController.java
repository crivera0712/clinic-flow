package com.clinicflow.clinic_flow.users;

import com.clinicflow.clinic_flow.users.dtos.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
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
    public UsersResponseDto getUser(@PathVariable @Positive Long userId) {
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

    @PatchMapping("/{id}")
    public ResponseEntity<UsersResponseDto>  updateUser(@PathVariable @Positive Long id,
            @Valid @RequestBody UserPatchDto userPatchDto) {
        var  updatedUser = usersService.updateUser(id, userPatchDto);
        return ResponseEntity.ok(updatedUser);
    }

}
