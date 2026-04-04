package com.medconnect.backend.controller;

import com.medconnect.backend.model.User;
import com.medconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public User register(@RequestBody User user) {

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Admin registration not allowed");
        }

        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }

        if ("DOCTOR".equalsIgnoreCase(user.getRole()) ||
            "PHARMACIST".equalsIgnoreCase(user.getRole())) {

            if (user.getLicenseId() == null || user.getLicenseId().trim().isEmpty()) {
                throw new RuntimeException("License ID required");
            }

            user.setApprovalStatus("PENDING");

        } else {
            user.setApprovalStatus("APPROVED");
            user.setSubscriptionPlan("FREE");
        }

        return userRepository.save(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody User loginUser) {

        User user = userRepository.findByUsername(loginUser.getUsername());

        if (user != null && user.getPassword().equals(loginUser.getPassword())) {
            return user;
        }

        return null;
    }
}
