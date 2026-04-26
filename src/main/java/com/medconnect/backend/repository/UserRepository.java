package com.medconnect.backend.repository;

import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByRoleAndStatus(Role role, UserStatus status);

    List<User> findBySpecializationContainingIgnoreCase(String specialization);

    List<User> findByRoleAndStatusAndSpecializationContainingIgnoreCase(Role role, UserStatus status, String specialization);

    List<User> findByStatus(UserStatus status);

    long countByRole(Role role);

    long countByStatus(UserStatus status);

    boolean existsByEmail(String email);
}
