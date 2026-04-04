package com.medconnect.backend.controller;

import com.medconnect.backend.model.User;
import com.medconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Set<String> ALLOWED_ROLES = Set.of("PATIENT", "DOCTOR", "PHARMACIST");

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        String username = trimToNull(user.getUsername());
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        String role = normalizeRole(user.getRole());
        if (role.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }
        if (!ALLOWED_ROLES.contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role; allowed: PATIENT, DOCTOR, PHARMACIST");
        }

        user.setUsername(username);
        user.setRole(role);

        if (userRepository.findByUsername(username) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        user.setSubscriptionPlan("FREE");

        switch (role) {
            case "DOCTOR" -> {
                requireNonBlank(user.getSpecialization(), "Specialization is required for doctors");
                requireNonBlank(user.getLicenseId(), "License ID is required for doctors");
                user.setSpecialization(user.getSpecialization().trim());
                user.setLicenseId(user.getLicenseId().trim());
                user.setApprovalStatus("PENDING");
            }
            case "PHARMACIST" -> {
                requireNonBlank(user.getLicenseId(), "License ID is required for pharmacists");
                user.setLicenseId(user.getLicenseId().trim());
                user.setApprovalStatus("PENDING");
            }
            case "PATIENT" -> user.setApprovalStatus("APPROVED");
        }

        return userRepository.save(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody User loginUser) {
        String username = trimToNull(loginUser.getUsername());
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (loginUser.getPassword() == null || loginUser.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        User user = userRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(loginUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return user;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private static String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        return role.trim().toUpperCase(Locale.ROOT);
    }

    private static void requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
