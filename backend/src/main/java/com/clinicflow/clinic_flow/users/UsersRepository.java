package com.clinicflow.clinic_flow.users;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Long> {

    List<Users> findAllByClinicId(Long clinicId);

    Optional<Users> findByUsername(String username);

    Optional<Users> findByUsernameAndClinicId(@NotBlank(message = "username required") String username, Long clinicId);

    Optional<Users> findByIdAndClinicId(Long id, Long clinicId);
}
