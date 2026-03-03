package com.medconnect.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.medconnect.backend.model.User;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // We only need to write the function name.
    // Spring Boot automatically writes the SQL query for us!
    
	// Find user by UserID (Login)
    User findByUserid(String userid);

    // Find all users who are Doctors (For the Dropdown list)
    List<User> findByRole(String role);
    
    long countByRole(String role); // To count users like "5 Doctors"
}