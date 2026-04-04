package com.medconnect.backend.repository;

import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    long countByRole(Role role);

    boolean existsByEmail(String email);
}
